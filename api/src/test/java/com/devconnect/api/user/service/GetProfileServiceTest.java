package com.devconnect.api.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.controller.response.RelationshipStatus;
import com.devconnect.api.friendship.service.core.FindFriendshipBetweenService;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.service.core.FindUserByIdService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class GetProfileServiceTest {

	@InjectMocks
	private GetProfileService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindUserByIdService findUserByIdService;

	@Mock
	private FindFriendshipBetweenService findFriendshipBetweenService;

	@Test
	@DisplayName("Should get a friend's profile")
	void shouldGetFriendProfile() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(FriendshipFactory.RECIPIENT_ID))
			.thenReturn(FriendshipFactory.getRecipient());
		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(FriendshipFactory.getAccepted()));

		ProfileResponse response = tested.get(FriendshipFactory.RECIPIENT_ID);

		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getId());
		assertEquals(RelationshipStatus.FRIENDS, response.getRelationship().getStatus());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should get profile without relationship")
	void shouldGetProfileWithoutRelationship() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(FriendshipFactory.THIRD_USER_ID))
			.thenReturn(FriendshipFactory.getThirdUser());
		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.THIRD_USER_ID))
			.thenReturn(Optional.empty());

		ProfileResponse response = tested.get(FriendshipFactory.THIRD_USER_ID);

		assertEquals(RelationshipStatus.NONE, response.getRelationship().getStatus());
		assertNull(response.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should get profile with sent request")
	void shouldGetProfileWithSentRequest() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(FriendshipFactory.RECIPIENT_ID))
			.thenReturn(FriendshipFactory.getRecipient());
		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(FriendshipFactory.getPending()));

		ProfileResponse response = tested.get(FriendshipFactory.RECIPIENT_ID);

		assertEquals(RelationshipStatus.REQUEST_SENT, response.getRelationship().getStatus());
	}

	@Test
	@DisplayName("Should return own profile when ID is authenticated user")
	void shouldReturnOwnProfileWhenIdIsAuthenticatedUser() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(FriendshipFactory.REQUESTER_ID))
			.thenReturn(FriendshipFactory.getRequester());
		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.REQUESTER_ID))
			.thenReturn(Optional.empty());

		ProfileResponse response = tested.get(FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.OWN_PROFILE, response.getRelationship().getStatus());
		assertNull(response.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should not get profile of a missing user")
	void shouldNotGetProfileOfMissingUser() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findUserByIdService.byId(99999L))
			.thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
					"User not found"));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.get(99999L));

		assertEquals("User not found", exception.getReason());
		verify(findFriendshipBetweenService, never()).between(any(), any());
	}
}
