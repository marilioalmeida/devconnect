package com.devconnect.api.like.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.LikeFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ListPostLikesServiceTest {

	@InjectMocks
	private ListPostLikesService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindPostByIdService findPostByIdService;

	@Mock
	private ValidatePostAccessService validatePostAccessService;

	@Mock
	private LikeRepository likeRepository;

	@Test
	@DisplayName("Should list who liked post")
	void shouldListWhoLikedPost() {

		Post post = PostFactory.getPublic();
		User authenticated = FriendshipFactory.getRequester();
		User likers = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(likeRepository.findByPost(anyLong(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(LikeFactory.getBy(likers))));

		Page<UserSummaryResponse> page = tested.list(PostFactory.ID_POST, PageRequest.of(0, 10));

		assertEquals(1, page.getTotalElements());
		assertEquals(likers.getId(), page.getContent().get(0).getId());
		assertEquals(likers.getFullName(), page.getContent().get(0).getFullName());
	}

	@Test
	@DisplayName("Should validate access before listing")
	void shouldValidateAccessBeforeListing() {

		Post post = PostFactory.getPrivate();
		User thirdUser = FriendshipFactory.getThirdUser();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findPostByIdService.byId(PostFactory.PRIVATE_POST_ID)).thenReturn(post);
		doThrow(ResponseStatusException.class)
				.when(validatePostAccessService).validateAccess(post, thirdUser);

		assertThrows(ResponseStatusException.class,
				() -> tested.list(PostFactory.PRIVATE_POST_ID, PageRequest.of(0, 10)));

		verify(likeRepository, never()).findByPost(anyLong(), any(Pageable.class));
	}

	@Test
	@DisplayName("Should not list when post does not exist")
	void shouldNotListWhenPostDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class,
				() -> tested.list(PostFactory.ID_POST, PageRequest.of(0, 10)));

		verify(likeRepository, never()).findByPost(anyLong(), any(Pageable.class));
	}
}
