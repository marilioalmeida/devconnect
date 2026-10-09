package com.devconnect.api.factories;

import java.time.LocalDateTime;

import com.devconnect.api.friendship.controller.request.FriendshipRequest;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.user.domain.User;

public class FriendshipFactory {

	public static final Long FRIENDSHIP_ID = 10L;
	public static final Long REQUESTER_ID = 1L;
	public static final Long RECIPIENT_ID = 2L;
	public static final Long THIRD_USER_ID = 3L;
	public static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2026, 1, 10, 9, 30);
	public static final LocalDateTime RESPONDED_AT = LocalDateTime.of(2026, 1, 11, 14, 0);

	public static User getRequester() {
		return UserFactory.getBuilder()
			.id(REQUESTER_ID)
			.fullName("Ana Requester")
			.email("ana@devconnect.com")
			.nickname("ana")
			.build();
	}

	public static User getRecipient() {
		return UserFactory.getBuilder()
			.id(RECIPIENT_ID)
			.fullName("Bruno Recipient")
			.email("bruno@devconnect.com")
			.nickname("bruno")
			.build();
	}

	public static User getThirdUser() {
		return UserFactory.getBuilder()
			.id(THIRD_USER_ID)
			.fullName("Carla Outsider")
			.email("carla@devconnect.com")
			.nickname("carla")
			.build();
	}

	public static Friendship.FriendshipBuilder getBuilder() {
		return Friendship.builder()
			.id(FRIENDSHIP_ID)
			.requester(getRequester())
			.recipient(getRecipient())
			.status(FriendshipStatus.PENDING)
			.requestedAt(REQUESTED_AT);
	}

	public static Friendship getPending() {
		return getBuilder().build();
	}

	public static Friendship getAccepted() {
		return getBuilder()
			.status(FriendshipStatus.ACCEPTED)
			.respondedAt(RESPONDED_AT)
			.build();
	}

	public static Friendship getPendingBetween(User requester, User recipient) {
		return getBuilder()
			.requester(requester)
			.recipient(recipient)
			.build();
	}

	public static Friendship getAcceptedBetween(User requester, User recipient) {
		return getBuilder()
			.requester(requester)
			.recipient(recipient)
			.status(FriendshipStatus.ACCEPTED)
			.respondedAt(RESPONDED_AT)
			.build();
	}

	public static FriendshipRequest getRequest() {
		return getRequest(RECIPIENT_ID);
	}

	public static FriendshipRequest getRequest(Long recipientId) {
		FriendshipRequest request = new FriendshipRequest();
		request.setRecipientId(recipientId);
		return request;
	}
}
