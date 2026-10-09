package com.devconnect.api.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.mapper.PostMapper;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ListFeedServiceTest {

	private static final Pageable PAGEABLE = PageRequest.of(0, 20);

	@InjectMocks
	private ListFeedService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private EnrichPostsService enrichPostsService;

	@Test
	@DisplayName("Should list the authenticated user's feed")
	void shouldListAuthenticatedUserFeed() {

		var authenticated = FriendshipFactory.getRequester();
		Page<Post> posts = new PageImpl<>(List.of(
			PostFactory.getPublic(),
			PostFactory.getPrivateBy(FriendshipFactory.getRecipient())));

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(postRepository.findFeed(FriendshipFactory.REQUESTER_ID, PAGEABLE)).thenReturn(posts);
		when(enrichPostsService.enrich(posts, authenticated))
			.thenReturn(posts.map(PostMapper::toResponse));

		Page<PostResponse> response = tested.list(PAGEABLE);

		assertEquals(2, response.getContent().size());
		assertEquals(PostFactory.ID_POST, response.getContent().get(0).getId());
		assertEquals(FriendshipFactory.REQUESTER_ID, response.getContent().get(0).getAuthor().getId());
		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getContent().get(1).getAuthor().getId());
	}

	@Test
	@DisplayName("Should enrich page returned by repository")
	void shouldEnrichPageReturnedByRepository() {

		var authenticated = FriendshipFactory.getRequester();
		Page<Post> posts = new PageImpl<>(List.of(PostFactory.getPublic()));

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(postRepository.findFeed(FriendshipFactory.REQUESTER_ID, PAGEABLE)).thenReturn(posts);
		when(enrichPostsService.enrich(posts, authenticated))
			.thenReturn(posts.map(PostMapper::toResponse));

		tested.list(PAGEABLE);

		verify(enrichPostsService).enrich(posts, authenticated);
	}

	@Test
	@DisplayName("Should ignore sort requested by client")
	void shouldIgnoreSortRequestedByClient() {

		Pageable withSort = PageRequest.of(0, 20, Sort.by("createdAt"));

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(postRepository.findFeed(FriendshipFactory.REQUESTER_ID, PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.list(withSort);

		verify(postRepository).findFeed(FriendshipFactory.REQUESTER_ID, PAGEABLE);
	}

	@Test
	@DisplayName("Should return empty page without posts")
	void shouldReturnEmptyPageWithoutPosts() {

		var authenticated = FriendshipFactory.getRequester();
		Page<Post> empty = new PageImpl<>(List.of());

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(postRepository.findFeed(FriendshipFactory.REQUESTER_ID, PAGEABLE)).thenReturn(empty);
		when(enrichPostsService.enrich(empty, authenticated))
			.thenReturn(empty.map(PostMapper::toResponse));

		Page<PostResponse> response = tested.list(PAGEABLE);

		assertTrue(response.getContent().isEmpty());
	}
}
