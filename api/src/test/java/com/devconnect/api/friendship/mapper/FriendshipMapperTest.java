package com.devconnect.api.friendship.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devconnect.api.friendship.controller.response.FriendResponse;
import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.controller.response.RelationshipStatus;
import com.devconnect.api.friendship.controller.response.FriendRequestResponse;
import com.devconnect.api.friendship.controller.response.RelationshipResponse;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;

class FriendshipMapperTest {

	@Test
	@DisplayName("Should map users to pending entity")
	void shouldMapUsersToPendingEntity() {

		User requester = FriendshipFactory.getRequester();
		User recipient = FriendshipFactory.getRecipient();

		Friendship friendship = FriendshipMapper.toEntity(requester, recipient, FriendshipFactory.REQUESTED_AT);

		assertNull(friendship.getId());
		assertSame(requester, friendship.getRequester());
		assertSame(recipient, friendship.getRecipient());
		assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
		assertEquals(FriendshipFactory.REQUESTED_AT, friendship.getRequestedAt());
		assertNull(friendship.getRespondedAt());
	}

	@Test
	@DisplayName("Should map pending friendship to response")
	void shouldMapPendingFriendshipToResponse() {

		Friendship friendship = FriendshipFactory.getPending();

		FriendshipResponse response = FriendshipMapper.toResponse(friendship);

		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getId());
		assertEquals(FriendshipStatus.PENDING, response.getStatus());
		assertEquals(FriendshipFactory.REQUESTED_AT, response.getRequestedAt());
		assertNull(response.getRespondedAt());
		assertEquals(FriendshipFactory.REQUESTER_ID, response.getRequester().getId());
		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getRecipient().getId());
	}

	@Test
	@DisplayName("Should map accepted friendship to response")
	void shouldMapAcceptedFriendshipToResponse() {

		Friendship friendship = FriendshipFactory.getAccepted();

		FriendshipResponse response = FriendshipMapper.toResponse(friendship);

		assertEquals(FriendshipStatus.ACCEPTED, response.getStatus());
		assertEquals(FriendshipFactory.RESPONDED_AT, response.getRespondedAt());
	}

	@Test
	@DisplayName("Should map to friend request response")
	void shouldMapToFriendRequestResponse() {

		Friendship friendship = FriendshipFactory.getPending();

		FriendRequestResponse response = FriendshipMapper.toFriendRequestResponse(friendship);

		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getId());
		assertEquals(FriendshipFactory.REQUESTED_AT, response.getRequestedAt());
		assertEquals(FriendshipFactory.REQUESTER_ID, response.getRequester().getId());
		assertEquals("Ana Requester", response.getRequester().getFullName());
	}

	@Test
	@DisplayName("Should return recipient as friend when authenticated is requester")
	void shouldReturnRecipientAsFriendWhenAuthenticatedIsRequester() {

		Friendship friendship = FriendshipFactory.getAccepted();

		FriendResponse response = FriendshipMapper.toFriendResponse(friendship, FriendshipFactory.REQUESTER_ID);

		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getId());
		assertEquals(FriendshipFactory.RESPONDED_AT, response.getFriendsSince());
		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getFriend().getId());
	}

	@Test
	@DisplayName("Should return requester as friend when authenticated is recipient")
	void shouldReturnRequesterAsFriendWhenAuthenticatedIsRecipient() {

		Friendship friendship = FriendshipFactory.getAccepted();

		FriendResponse response = FriendshipMapper.toFriendResponse(friendship, FriendshipFactory.RECIPIENT_ID);

		assertEquals(FriendshipFactory.REQUESTER_ID, response.getFriend().getId());
	}

	@Test
	@DisplayName("Should map accepted friendship to friends relationship")
	void shouldMapAcceptedFriendshipToFriendsRelationship() {

		Friendship friendship = FriendshipFactory.getAccepted();

		RelationshipResponse response = FriendshipMapper.toRelationshipResponse(friendship, FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.FRIENDS, response.getStatus());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getFriendshipId());
	}

	@Test
	@DisplayName("Should map pending to request sent for requester")
	void shouldMapPendingToRequestSentForRequester() {

		Friendship friendship = FriendshipFactory.getPending();

		RelationshipResponse response = FriendshipMapper.toRelationshipResponse(friendship, FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.REQUEST_SENT, response.getStatus());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getFriendshipId());
	}

	@Test
	@DisplayName("Should map pending to request received for recipient")
	void shouldMapPendingToRequestReceivedForRecipient() {

		Friendship friendship = FriendshipFactory.getPending();

		RelationshipResponse response = FriendshipMapper.toRelationshipResponse(friendship, FriendshipFactory.RECIPIENT_ID);

		assertEquals(RelationshipStatus.REQUEST_RECEIVED, response.getStatus());
	}

	@Test
	@DisplayName("Should return none when friendship is null")
	void shouldReturnNoneWhenFriendshipIsNull() {

		RelationshipResponse response = FriendshipMapper.toRelationshipResponse(null, FriendshipFactory.REQUESTER_ID);

		assertNotNull(response);
		assertEquals(RelationshipStatus.NONE, response.getStatus());
		assertNull(response.getFriendshipId());
	}
}
