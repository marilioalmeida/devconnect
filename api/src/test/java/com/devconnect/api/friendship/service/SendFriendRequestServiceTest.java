package com.devconnect.api.friendship.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.controller.request.FriendshipRequest;
import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.friendship.service.core.ValidateNewFriendshipService;
import com.devconnect.api.core.service.NowService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.FindUserByIdService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class SendFriendRequestServiceTest {

	@InjectMocks
	private SendFriendRequestService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindUserByIdService findUserByIdService;

	@Mock
	private ValidateNewFriendshipService validateNewFriendshipService;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Mock
	private NowService nowService;

	@Captor
	private ArgumentCaptor<Friendship> friendshipCaptor;

	@Test
	@DisplayName("Should send friend request")
	void shouldSendFriendRequest() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();
		FriendshipRequest request = FriendshipFactory.getRequest();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(findUserByIdService.byId(FriendshipFactory.RECIPIENT_ID)).thenReturn(recipient);
		when(nowService.getDateTime()).thenReturn(FriendshipFactory.REQUESTED_AT);
		when(friendshipRepository.saveAndFlush(any(Friendship.class))).thenReturn(FriendshipFactory.getPending());

		FriendshipResponse response = tested.send(request);

		verify(friendshipRepository).saveAndFlush(friendshipCaptor.capture());

		Friendship friendship = friendshipCaptor.getValue();
		assertNull(friendship.getId());
		assertSame(requester, friendship.getRequester());
		assertSame(recipient, friendship.getRecipient());
		assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
		assertEquals(FriendshipFactory.REQUESTED_AT, friendship.getRequestedAt());
		assertNull(friendship.getRespondedAt());

		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getId());
	}

	@Test
	@DisplayName("Should send friend request running validations")
	void shouldSendFriendRequestRunningValidations() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(findUserByIdService.byId(FriendshipFactory.RECIPIENT_ID)).thenReturn(recipient);
		when(nowService.getDateTime()).thenReturn(FriendshipFactory.REQUESTED_AT);
		when(friendshipRepository.saveAndFlush(any(Friendship.class))).thenReturn(FriendshipFactory.getPending());

		tested.send(FriendshipFactory.getRequest());

		verify(validateNewFriendshipService).validate(requester, recipient);
	}

	@Test
	@DisplayName("Should not send request when validation fails")
	void shouldNotSendRequestWhenValidationFails() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(findUserByIdService.byId(FriendshipFactory.RECIPIENT_ID)).thenReturn(recipient);
		doThrow(ResponseStatusException.class)
				.when(validateNewFriendshipService).validate(requester, recipient);

		assertThrows(ResponseStatusException.class, () -> tested.send(FriendshipFactory.getRequest()));

		verify(friendshipRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should not send request when recipient does not exist")
	void shouldNotSendRequestWhenRecipientDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(FriendshipFactory.RECIPIENT_ID))
			.thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.send(FriendshipFactory.getRequest()));

		verify(validateNewFriendshipService, never()).validate(any(), any());
		verify(friendshipRepository, never()).save(any());
	}
}
