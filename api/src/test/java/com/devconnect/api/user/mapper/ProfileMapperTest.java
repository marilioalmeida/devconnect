package com.devconnect.api.user.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devconnect.api.friendship.controller.response.RelationshipStatus;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.UserFactory;
import com.devconnect.api.user.domain.User;

class ProfileMapperTest {

	@Test
	@DisplayName("Should map user with public data")
	void shouldMapUserWithPublicData() {

		User user = FriendshipFactory.getRecipient();

		ProfileResponse response = ProfileMapper.toResponse(user, null, FriendshipFactory.REQUESTER_ID);

		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getId());
		assertEquals("Bruno Recipient", response.getFullName());
		assertEquals("bruno", response.getNickname());
		assertEquals(user.getProfileImage(), response.getProfileImage());
	}

	@Test
	@DisplayName("Should map with friends status")
	void shouldMapWithFriendsStatus() {

		ProfileResponse response = ProfileMapper.toResponse(
			FriendshipFactory.getRecipient(), FriendshipFactory.getAccepted(), FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.FRIENDS, response.getRelationship().getStatus());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should map with none status when friendship is null")
	void shouldMapWithNoneStatusWhenFriendshipIsNull() {

		ProfileResponse response = ProfileMapper.toResponse(
			FriendshipFactory.getThirdUser(), null, FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.NONE, response.getRelationship().getStatus());
		assertNull(response.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should identify sent request")
	void shouldIdentifySentRequest() {

		ProfileResponse response = ProfileMapper.toResponse(
			FriendshipFactory.getRecipient(), FriendshipFactory.getPending(), FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.REQUEST_SENT, response.getRelationship().getStatus());
	}

	@Test
	@DisplayName("Should identify received request")
	void shouldIdentifyReceivedRequest() {

		ProfileResponse response = ProfileMapper.toResponse(
			FriendshipFactory.getRequester(), FriendshipFactory.getPending(), FriendshipFactory.RECIPIENT_ID);

		assertEquals(RelationshipStatus.REQUEST_RECEIVED, response.getRelationship().getStatus());
	}

	@Test
	@DisplayName("Should identify own profile")
	void shouldIdentifyOwnProfile() {

		ProfileResponse response = ProfileMapper.toResponse(
			FriendshipFactory.getRequester(), FriendshipFactory.getAccepted(), FriendshipFactory.REQUESTER_ID);

		assertEquals(RelationshipStatus.OWN_PROFILE, response.getRelationship().getStatus());
		assertNull(response.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should map user without nickname and image")
	void shouldMapUserWithoutNicknameAndImage() {

		User user = UserFactory.getBuilder()
			.id(FriendshipFactory.THIRD_USER_ID)
			.nickname(null)
			.profileImage(null)
			.build();

		ProfileResponse response = ProfileMapper.toResponse(user, null, FriendshipFactory.REQUESTER_ID);

		assertNull(response.getNickname());
		assertNull(response.getProfileImage());
		assertEquals(FriendshipFactory.THIRD_USER_ID, response.getId());
	}
}
