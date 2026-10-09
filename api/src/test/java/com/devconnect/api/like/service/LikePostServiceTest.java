package com.devconnect.api.like.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.core.service.NowService;
import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.domain.PostLike;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.LikeFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class LikePostServiceTest {

	@InjectMocks
	private LikePostService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindPostByIdService findPostByIdService;

	@Mock
	private ValidatePostAccessService validatePostAccessService;

	@Mock
	private LikeRepository likeRepository;

	@Mock
	private NowService nowService;

	@Captor
	private ArgumentCaptor<PostLike> likeCaptor;

	@Test
	@DisplayName("Should like post")
	void shouldLikePost() {

		Post post = PostFactory.getPublic();
		User authenticated = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(likeRepository.existsByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(false);
		when(nowService.getDateTime()).thenReturn(LikeFactory.CREATED_AT);
		when(likeRepository.countByPostId(PostFactory.ID_POST)).thenReturn(1L);

		tested.like(PostFactory.ID_POST);

		verify(likeRepository).saveAndFlush(likeCaptor.capture());

		PostLike like = likeCaptor.getValue();
		assertNull(like.getId());
		assertSame(post, like.getPost());
		assertSame(authenticated, like.getUser());
		assertEquals(LikeFactory.CREATED_AT, like.getCreatedAt());
	}

	@Test
	@DisplayName("Should return updated summary")
	void shouldReturnUpdatedSummary() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(likeRepository.existsByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(false);
		when(nowService.getDateTime()).thenReturn(LikeFactory.CREATED_AT);
		when(likeRepository.countByPostId(PostFactory.ID_POST)).thenReturn(7L);

		LikeSummaryResponse response = tested.like(PostFactory.ID_POST);

		assertEquals(PostFactory.ID_POST, response.getPostId());
		assertEquals(7L, response.getLikeCount());
		assertTrue(response.isLikedByCurrentUser());
	}

	@Test
	@DisplayName("Should validate access before liking")
	void shouldValidateAccessBeforeLiking() {

		Post post = PostFactory.getPublic();
		User authenticated = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(likeRepository.existsByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(false);
		when(nowService.getDateTime()).thenReturn(LikeFactory.CREATED_AT);
		when(likeRepository.countByPostId(PostFactory.ID_POST)).thenReturn(1L);

		tested.like(PostFactory.ID_POST);

		verify(validatePostAccessService).validateAccess(post, authenticated);
	}

	@Test
	@DisplayName("Should not like when post does not exist")
	void shouldNotLikeWhenPostDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.like(PostFactory.ID_POST));

		verify(likeRepository, never()).saveAndFlush(any());
	}

	@Test
	@DisplayName("Should not like without post access")
	void shouldNotLikeWithoutPostAccess() {

		Post post = PostFactory.getPrivate();
		User thirdUser = FriendshipFactory.getThirdUser();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findPostByIdService.byId(PostFactory.PRIVATE_POST_ID)).thenReturn(post);
		doThrow(ResponseStatusException.class)
				.when(validatePostAccessService).validateAccess(post, thirdUser);

		assertThrows(ResponseStatusException.class, () -> tested.like(PostFactory.PRIVATE_POST_ID));

		verify(likeRepository, never()).saveAndFlush(any());
	}

	@Test
	@DisplayName("Should not like twice")
	void shouldNotLikeTwice() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(likeRepository.existsByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(true);

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.like(PostFactory.ID_POST));

		assertEquals("You have already liked this post", exception.getReason());
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(likeRepository, never()).saveAndFlush(any());
	}

	@Test
	@DisplayName("Should fail when uniqueness is violated")
	void shouldFailWhenUniquenessIsViolated() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(likeRepository.existsByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(false);
		when(nowService.getDateTime()).thenReturn(LikeFactory.CREATED_AT);
		when(likeRepository.saveAndFlush(any(PostLike.class)))
			.thenThrow(new DataIntegrityViolationException("uk_post_likes_post_user"));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.like(PostFactory.ID_POST));

		assertEquals("You have already liked this post", exception.getReason());
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(likeRepository, never()).countByPostId(any());
	}
}
