package com.devconnect.api.like.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.domain.PostLike;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.LikeFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class UnlikePostServiceTest {

	@InjectMocks
	private UnlikePostService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private LikeRepository likeRepository;

	@Test
	@DisplayName("Should remove like")
	void shouldRemoveLike() {

		PostLike like = LikeFactory.getLike();

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(likeRepository.findByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(Optional.of(like));
		when(likeRepository.countByPostId(PostFactory.ID_POST)).thenReturn(0L);

		tested.unlike(PostFactory.ID_POST);

		verify(likeRepository).delete(like);
	}

	@Test
	@DisplayName("Should return updated summary")
	void shouldReturnUpdatedSummary() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(likeRepository.findByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(LikeFactory.getLike()));
		when(likeRepository.countByPostId(PostFactory.ID_POST)).thenReturn(2L);

		LikeSummaryResponse response = tested.unlike(PostFactory.ID_POST);

		assertEquals(PostFactory.ID_POST, response.getPostId());
		assertEquals(2L, response.getLikeCount());
		assertFalse(response.isLikedByCurrentUser());
	}

	@Test
	@DisplayName("Should find like by post and user")
	void shouldFindLikeByPostAndUser() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(likeRepository.findByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(LikeFactory.getLike()));
		when(likeRepository.countByPostId(PostFactory.ID_POST)).thenReturn(0L);

		tested.unlike(PostFactory.ID_POST);

		verify(likeRepository).findByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID);
	}

	@Test
	@DisplayName("Should fail when there is no like")
	void shouldFailWhenThereIsNoLike() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(likeRepository.findByPostIdAndUserId(
			PostFactory.ID_POST, FriendshipFactory.RECIPIENT_ID)).thenReturn(Optional.empty());

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.unlike(PostFactory.ID_POST));

		assertEquals("Like not found", exception.getReason());
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		verify(likeRepository, never()).delete(any());
	}
}
