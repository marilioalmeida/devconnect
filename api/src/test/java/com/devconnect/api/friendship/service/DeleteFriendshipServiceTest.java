package com.devconnect.api.friendship.service;

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
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.friendship.service.core.FindFriendshipByIdService;
import com.devconnect.api.friendship.service.core.ValidateFriendshipParticipantService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class DeleteFriendshipServiceTest {

	@InjectMocks
	private DeleteFriendshipService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindFriendshipByIdService findFriendshipByIdService;

	@Mock
	private ValidateFriendshipParticipantService validateFriendshipParticipantService;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Test
	@DisplayName("Should undo accepted friendship")
	void shouldUndoAcceptedFriendship() {

		User user = FriendshipFactory.getRequester();
		Friendship friendship = FriendshipFactory.getAccepted();

		when(authenticatedUserService.get()).thenReturn(user);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);

		tested.delete(FriendshipFactory.FRIENDSHIP_ID);

		verify(validateFriendshipParticipantService).validateParticipant(friendship, user);
		verify(friendshipRepository).delete(friendship);
	}

	@Test
	@DisplayName("Should decline pending request")
	void shouldDeclinePendingRequest() {

		User recipient = FriendshipFactory.getRecipient();
		Friendship friendship = FriendshipFactory.getPending();

		when(authenticatedUserService.get()).thenReturn(recipient);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);

		tested.delete(FriendshipFactory.FRIENDSHIP_ID);

		verify(friendshipRepository).delete(friendship);
	}

	@Test
	@DisplayName("Should cancel sent request")
	void shouldCancelSentRequest() {

		User requester = FriendshipFactory.getRequester();
		Friendship friendship = FriendshipFactory.getPending();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);

		tested.delete(FriendshipFactory.FRIENDSHIP_ID);

		verify(friendshipRepository).delete(friendship);
	}

	@Test
	@DisplayName("Should not delete a third user's friendship")
	void shouldNotDeleteThirdUsersFriendship() {

		User thirdUser = FriendshipFactory.getThirdUser();
		Friendship friendship = FriendshipFactory.getAccepted();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(friendship);
		doThrow(ResponseStatusException.class)
				.when(validateFriendshipParticipantService).validateParticipant(friendship, thirdUser);

		assertThrows(ResponseStatusException.class, () -> tested.delete(FriendshipFactory.FRIENDSHIP_ID));

		verify(friendshipRepository, never()).delete(any());
	}

	@Test
	@DisplayName("Should not delete a missing friendship")
	void shouldNotDeleteMissingFriendship() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findFriendshipByIdService.byId(FriendshipFactory.FRIENDSHIP_ID)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.delete(FriendshipFactory.FRIENDSHIP_ID));

		verify(friendshipRepository, never()).delete(any());
	}
}
