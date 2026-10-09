package com.devconnect.api.friendship.mapper;

import java.time.LocalDateTime;

import com.devconnect.api.friendship.controller.response.FriendResponse;
import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.controller.response.RelationshipStatus;
import com.devconnect.api.friendship.controller.response.FriendRequestResponse;
import com.devconnect.api.friendship.controller.response.RelationshipResponse;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.mapper.UserMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FriendshipMapper {

	public Friendship toEntity(User requester, User recipient, LocalDateTime requestedAt) {
		return Friendship.builder()
			.requester(requester)
			.recipient(recipient)
			.status(FriendshipStatus.PENDING)
			.requestedAt(requestedAt)
			.build();
	}

	public FriendshipResponse toResponse(Friendship friendship) {
		return FriendshipResponse.builder()
			.id(friendship.getId())
			.status(friendship.getStatus())
			.requestedAt(friendship.getRequestedAt())
			.respondedAt(friendship.getRespondedAt())
			.requester(UserMapper.toSummaryResponse(friendship.getRequester()))
			.recipient(UserMapper.toSummaryResponse(friendship.getRecipient()))
			.build();
	}

	public FriendRequestResponse toFriendRequestResponse(Friendship friendship) {
		return FriendRequestResponse.builder()
			.id(friendship.getId())
			.requestedAt(friendship.getRequestedAt())
			.requester(UserMapper.toSummaryResponse(friendship.getRequester()))
			.build();
	}

	public FriendResponse toFriendResponse(Friendship friendship, Long authenticatedUserId) {
		return FriendResponse.builder()
			.id(friendship.getId())
			.friendsSince(friendship.getRespondedAt())
			.friend(UserMapper.toSummaryResponse(getOtherParticipant(friendship, authenticatedUserId)))
			.build();
	}

	public RelationshipResponse toRelationshipResponse(Friendship friendship, Long authenticatedUserId) {
		if (friendship == null) {
			return RelationshipResponse.builder()
				.status(RelationshipStatus.NONE)
				.build();
		}

		return RelationshipResponse.builder()
			.status(resolveStatus(friendship, authenticatedUserId))
			.friendshipId(friendship.getId())
			.build();
	}

	private User getOtherParticipant(Friendship friendship, Long authenticatedUserId) {
		return friendship.getRequester().getId().equals(authenticatedUserId)
			? friendship.getRecipient()
			: friendship.getRequester();
	}

	private RelationshipStatus resolveStatus(Friendship friendship, Long authenticatedUserId) {
		if (FriendshipStatus.ACCEPTED.equals(friendship.getStatus())) {
			return RelationshipStatus.FRIENDS;
		}

		return friendship.getRequester().getId().equals(authenticatedUserId)
			? RelationshipStatus.REQUEST_SENT
			: RelationshipStatus.REQUEST_RECEIVED;
	}

}
