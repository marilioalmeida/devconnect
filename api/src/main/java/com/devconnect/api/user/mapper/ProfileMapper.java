package com.devconnect.api.user.mapper;

import com.devconnect.api.friendship.controller.response.RelationshipStatus;
import com.devconnect.api.friendship.controller.response.RelationshipResponse;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.mapper.FriendshipMapper;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.user.domain.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProfileMapper {

	public ProfileResponse toResponse(User user, Friendship relationship, Long authenticatedUserId) {
		return ProfileResponse.builder()
			.id(user.getId())
			.fullName(user.getFullName())
			.nickname(user.getNickname())
			.profileImage(user.getProfileImage())
			.relationship(resolveRelationship(user, relationship, authenticatedUserId))
			.build();
	}

	private RelationshipResponse resolveRelationship(User user, Friendship relationship, Long authenticatedUserId) {
		if (user.getId().equals(authenticatedUserId)) {
			return RelationshipResponse.builder()
				.status(RelationshipStatus.OWN_PROFILE)
				.build();
		}

		return FriendshipMapper.toRelationshipResponse(relationship, authenticatedUserId);
	}

}
