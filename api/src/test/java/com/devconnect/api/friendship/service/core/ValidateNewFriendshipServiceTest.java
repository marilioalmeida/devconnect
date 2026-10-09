package com.devconnect.api.friendship.service.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;

@ExtendWith(MockitoExtension.class)
class ValidateNewFriendshipServiceTest {

	@InjectMocks
	private ValidateNewFriendshipService tested;

	@Mock
	private FindFriendshipBetweenService findFriendshipBetweenService;

	@Test
	@DisplayName("Should validate new friendship")
	void shouldValidateNewFriendship() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();

		when(findFriendshipBetweenService.between(requester.getId(), recipient.getId()))
			.thenReturn(Optional.empty());

		assertDoesNotThrow(() -> tested.validate(requester, recipient));

		verify(findFriendshipBetweenService).between(requester.getId(), recipient.getId());
	}

	@Test
	@DisplayName("Should reject request to self")
	void shouldRejectRequestToSelf() {

		User requester = FriendshipFactory.getRequester();

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.validate(requester, requester));

		assertEquals("You cannot send a friend request to yourself", exception.getReason());
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
		verify(findFriendshipBetweenService, never()).between(any(), any());
	}

	@Test
	@DisplayName("Should reject when pending request exists")
	void shouldRejectWhenPendingRequestExists() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();

		when(findFriendshipBetweenService.between(requester.getId(), recipient.getId()))
			.thenReturn(Optional.of(FriendshipFactory.getPending()));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.validate(requester, recipient));

		assertEquals("There is already a pending friend request with this user", exception.getReason());
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	@Test
	@DisplayName("Should reject when already friends")
	void shouldRejectWhenAlreadyFriends() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();

		when(findFriendshipBetweenService.between(requester.getId(), recipient.getId()))
			.thenReturn(Optional.of(FriendshipFactory.getAccepted()));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.validate(requester, recipient));

		assertEquals("You are already friends", exception.getReason());
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	@Test
	@DisplayName("Should reject when pending request exists in reverse")
	void shouldRejectWhenPendingRequestExistsInReverse() {

		User requester = FriendshipFactory.getRecipient();
		User recipient = FriendshipFactory.getRequester();

		when(findFriendshipBetweenService.between(requester.getId(), recipient.getId()))
			.thenReturn(Optional.of(FriendshipFactory.getPending()));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.validate(requester, recipient));

		assertEquals("There is already a pending friend request with this user", exception.getReason());
	}
}
