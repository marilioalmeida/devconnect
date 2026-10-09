package com.devconnect.api.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.post.mapper.PostMapper;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.post.service.core.ResolveAllowedVisibilitiesService;
import com.devconnect.api.user.service.core.FindUserByIdService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ListProfilePostsServiceTest {

	private static final Pageable PAGEABLE = PageRequest.of(0, 20);
	private static final List<Visibility> ALL = List.of(Visibility.PUBLIC, Visibility.PRIVATE);
	private static final List<Visibility> PUBLIC_ONLY = List.of(Visibility.PUBLIC);

	@InjectMocks
	private ListProfilePostsService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindUserByIdService findUserByIdService;

	@Mock
	private ResolveAllowedVisibilitiesService resolveAllowedVisibilitiesService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private EnrichPostsService enrichPostsService;

	@Test
	@DisplayName("Should list all posts on own profile")
	void shouldListAllPostsOnOwnProfile() {

		var authenticated = FriendshipFactory.getRequester();
		Page<Post> posts = new PageImpl<>(List.of(PostFactory.getPublic(), PostFactory.getPrivate()));

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findUserByIdService.byId(FriendshipFactory.REQUESTER_ID)).thenReturn(authenticated);
		when(resolveAllowedVisibilitiesService.allowed(authenticated, authenticated)).thenReturn(ALL);
		when(postRepository.findByAuthor(FriendshipFactory.REQUESTER_ID, ALL, PAGEABLE)).thenReturn(posts);
		when(enrichPostsService.enrich(posts, authenticated))
			.thenReturn(posts.map(PostMapper::toResponse));

		Page<PostResponse> response = tested.list(FriendshipFactory.REQUESTER_ID, PAGEABLE);

		assertEquals(2, response.getContent().size());
		verify(postRepository).findByAuthor(FriendshipFactory.REQUESTER_ID, ALL, PAGEABLE);
	}

	@Test
	@DisplayName("Should list public and private posts with accepted friendship")
	void shouldListPublicAndPrivatePostsWithAcceptedFriendship() {

		var authenticated = FriendshipFactory.getRecipient();
		var profileOwner = FriendshipFactory.getRequester();
		Page<Post> posts = new PageImpl<>(List.of(PostFactory.getPublic(), PostFactory.getPrivate()));

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findUserByIdService.byId(FriendshipFactory.REQUESTER_ID)).thenReturn(profileOwner);
		when(resolveAllowedVisibilitiesService.allowed(authenticated, profileOwner)).thenReturn(ALL);
		when(postRepository.findByAuthor(FriendshipFactory.REQUESTER_ID, ALL, PAGEABLE)).thenReturn(posts);
		when(enrichPostsService.enrich(posts, authenticated))
			.thenReturn(posts.map(PostMapper::toResponse));

		Page<PostResponse> response = tested.list(FriendshipFactory.REQUESTER_ID, PAGEABLE);

		assertEquals(2, response.getContent().size());
	}

	@Test
	@DisplayName("Should list only public posts without friendship")
	void shouldListOnlyPublicPostsWithoutFriendship() {

		var authenticated = FriendshipFactory.getThirdUser();
		var profileOwner = FriendshipFactory.getRequester();
		Page<Post> posts = new PageImpl<>(List.of(PostFactory.getPublic()));

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findUserByIdService.byId(FriendshipFactory.REQUESTER_ID)).thenReturn(profileOwner);
		when(resolveAllowedVisibilitiesService.allowed(authenticated, profileOwner))
			.thenReturn(PUBLIC_ONLY);
		when(postRepository.findByAuthor(FriendshipFactory.REQUESTER_ID, PUBLIC_ONLY, PAGEABLE))
			.thenReturn(posts);
		when(enrichPostsService.enrich(posts, authenticated))
			.thenReturn(posts.map(PostMapper::toResponse));

		Page<PostResponse> response = tested.list(FriendshipFactory.REQUESTER_ID, PAGEABLE);

		assertEquals(1, response.getContent().size());
		assertEquals(Visibility.PUBLIC, response.getContent().get(0).getVisibility());
		verify(postRepository).findByAuthor(FriendshipFactory.REQUESTER_ID, PUBLIC_ONLY, PAGEABLE);
	}

	@Test
	@DisplayName("Should not list when profile user does not exist")
	void shouldNotListWhenProfileUserDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(99999L)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.list(99999L, PAGEABLE));

		verify(resolveAllowedVisibilitiesService, never()).allowed(any(), any());
		verify(postRepository, never()).findByAuthor(anyLong(), any(), any());
		verify(enrichPostsService, never()).enrich(any(Page.class), any());
	}

	@Test
	@DisplayName("Should ignore sort requested by client")
	void shouldIgnoreSortRequestedByClient() {

		Pageable withSort = PageRequest.of(0, 20, Sort.by("createdAt"));
		var authenticated = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findUserByIdService.byId(FriendshipFactory.REQUESTER_ID)).thenReturn(authenticated);
		when(resolveAllowedVisibilitiesService.allowed(authenticated, authenticated)).thenReturn(ALL);
		when(postRepository.findByAuthor(FriendshipFactory.REQUESTER_ID, ALL, PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.list(FriendshipFactory.REQUESTER_ID, withSort);

		verify(postRepository).findByAuthor(FriendshipFactory.REQUESTER_ID, ALL, PAGEABLE);
	}
}
