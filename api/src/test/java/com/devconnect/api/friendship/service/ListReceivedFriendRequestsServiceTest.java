package com.devconnect.api.friendship.service;

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

import com.devconnect.api.friendship.controller.response.FriendRequestResponse;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ListReceivedFriendRequestsServiceTest {

	@InjectMocks
	private ListReceivedFriendRequestsService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Test
	@DisplayName("Should list received requests")
	void shouldListReceivedRequests() {

		User recipient = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(recipient);
		when(friendshipRepository.findReceivedRequests(FriendshipFactory.RECIPIENT_ID))
			.thenReturn(List.of(
				FriendshipFactory.getPending(),
				FriendshipFactory.getPendingBetween(FriendshipFactory.getThirdUser(), recipient)));

		List<FriendRequestResponse> response = tested.list();

		assertEquals(2, response.size());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.get(0).getId());
		assertEquals(FriendshipFactory.REQUESTER_ID, response.get(0).getRequester().getId());
		assertEquals(FriendshipFactory.THIRD_USER_ID, response.get(1).getRequester().getId());
	}

	@Test
	@DisplayName("Should return empty list without requests")
	void shouldReturnEmptyListWithoutRequests() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(friendshipRepository.findReceivedRequests(FriendshipFactory.RECIPIENT_ID))
			.thenReturn(List.of());

		List<FriendRequestResponse> response = tested.list();

		assertTrue(response.isEmpty());
	}

	@Test
	@DisplayName("Should query requests of authenticated user")
	void shouldQueryRequestsOfAuthenticatedUser() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(friendshipRepository.findReceivedRequests(FriendshipFactory.RECIPIENT_ID))
			.thenReturn(List.of());

		tested.list();

		verify(friendshipRepository).findReceivedRequests(FriendshipFactory.RECIPIENT_ID);
	}
}
