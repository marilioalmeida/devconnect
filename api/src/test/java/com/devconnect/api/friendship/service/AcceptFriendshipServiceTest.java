package com.devconnect.api.friendship.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.friendship.service.core.FindFriendshipByIdService;
import com.devconnect.api.friendship.service.core.ValidateFriendshipParticipantService;
import com.devconnect.api.core.service.NowService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class AcceptFriendshipServiceTest {

	@InjectMocks
	private AcceptFriendshipService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindFriendshipByIdService findFriendshipByIdService;

	@Mock
	private ValidateFriendshipParticipantService validateFriendshipParticipantService;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Mock
	private NowService nowService;

	@Test
	@DisplayName("Should accept request")
	void shouldAcceptRequest() {

		User recipient = FriendshipFactory.getRecipient();
		Friendship friendship = FriendshipFactory.getPending();

		when(authenticatedUserService.get()).thenReturn(recipient);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);
		when(nowService.getDateTime()).thenReturn(FriendshipFactory.RESPONDED_AT);
		when(friendshipRepository.save(friendship)).thenReturn(friendship);

		FriendshipResponse response = tested.accept(FriendshipFactory.FRIENDSHIP_ID);

		assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
		assertEquals(FriendshipFactory.RESPONDED_AT, friendship.getRespondedAt());

		verify(friendshipRepository).save(friendship);
		assertEquals(FriendshipStatus.ACCEPTED, response.getStatus());
		assertEquals(FriendshipFactory.RESPONDED_AT, response.getRespondedAt());
	}

	@Test
	@DisplayName("Should accept request after validating recipient")
	void shouldAcceptRequestAfterValidatingRecipient() {

		User recipient = FriendshipFactory.getRecipient();
		Friendship friendship = FriendshipFactory.getPending();

		when(authenticatedUserService.get()).thenReturn(recipient);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);
		when(nowService.getDateTime()).thenReturn(FriendshipFactory.RESPONDED_AT);
		when(friendshipRepository.save(friendship)).thenReturn(friendship);

		tested.accept(FriendshipFactory.FRIENDSHIP_ID);

		verify(validateFriendshipParticipantService).validateRecipient(friendship, recipient);
	}

	@Test
	@DisplayName("Should not accept another user's request")
	void shouldNotAcceptAnotherUsersRequest() {

		User requester = FriendshipFactory.getRequester();
		Friendship friendship = FriendshipFactory.getPending();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);
		doThrow(ResponseStatusException.class)
				.when(validateFriendshipParticipantService).validateRecipient(friendship, requester);

		assertThrows(ResponseStatusException.class, () -> tested.accept(FriendshipFactory.FRIENDSHIP_ID));

		verify(friendshipRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should not accept already accepted request")
	void shouldNotAcceptAlreadyAcceptedRequest() {

		User recipient = FriendshipFactory.getRecipient();
		Friendship friendship = FriendshipFactory.getAccepted();

		when(authenticatedUserService.get()).thenReturn(recipient);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.accept(FriendshipFactory.FRIENDSHIP_ID));

		assertEquals("This friend request has already been accepted", exception.getReason());
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		verify(friendshipRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should not accept a missing friendship")
	void shouldNotAcceptMissingFriendship() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.accept(FriendshipFactory.FRIENDSHIP_ID));

		verify(friendshipRepository, never()).save(any());
	}
}
