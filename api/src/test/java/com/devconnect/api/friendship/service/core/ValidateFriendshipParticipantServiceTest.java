package com.devconnect.api.friendship.service.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.factories.FriendshipFactory;

@ExtendWith(MockitoExtension.class)
class ValidateFriendshipParticipantServiceTest {

	@InjectMocks
	private ValidateFriendshipParticipantService tested;

	@Test
	@DisplayName("Should validate requester as participant")
	void shouldValidateRequesterAsParticipant() {

		Friendship friendship = FriendshipFactory.getAccepted();

		assertDoesNotThrow(() -> tested.validateParticipant(friendship, FriendshipFactory.getRequester()));
	}

	@Test
	@DisplayName("Should validate recipient as participant")
	void shouldValidateRecipientAsParticipant() {

		Friendship friendship = FriendshipFactory.getAccepted();

		assertDoesNotThrow(() -> tested.validateParticipant(friendship, FriendshipFactory.getRecipient()));
	}

	@Test
	@DisplayName("Should reject participant check when user is not participant")
	void shouldRejectParticipantCheckWhenUserIsNotParticipant() {

		Friendship friendship = FriendshipFactory.getAccepted();

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.validateParticipant(friendship, FriendshipFactory.getThirdUser()));

		assertEquals("You are not part of this friendship", exception.getReason());
		assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
	}

	@Test
	@DisplayName("Should validate recipient")
	void shouldValidateRecipient() {

		Friendship friendship = FriendshipFactory.getPending();

		assertDoesNotThrow(() -> tested.validateRecipient(friendship, FriendshipFactory.getRecipient()));
	}

	@Test
	@DisplayName("Should reject recipient check when user is requester")
	void shouldRejectRecipientCheckWhenUserIsRequester() {

		Friendship friendship = FriendshipFactory.getPending();

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.validateRecipient(friendship, FriendshipFactory.getRequester()));

		assertEquals("Only the recipient can accept the friend request", exception.getReason());
		assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
	}

	@Test
	@DisplayName("Should reject recipient check when user is not participant")
	void shouldRejectRecipientCheckWhenUserIsNotParticipant() {

		Friendship friendship = FriendshipFactory.getPending();

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.validateRecipient(friendship, FriendshipFactory.getThirdUser()));

		assertEquals("Only the recipient can accept the friend request", exception.getReason());
	}
}
