package com.devconnect.api.user.mapper;

import com.devconnect.api.core.text.Texts;
import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import com.devconnect.api.user.domain.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserMapper {

	public User toEntity(UserRequest request, String encodedPassword) {
		return User.builder()
			.fullName(Texts.required(request.getFullName()))
			.email(Texts.email(request.getEmail()))
			.nickname(Texts.optional(request.getNickname()))
			.birthDate(request.getBirthDate())
			.password(encodedPassword)
			.profileImage(Texts.optional(request.getProfileImage()))
			.active(true)
			.build();
	}

	public UserResponse toResponse(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.fullName(user.getFullName())
			.email(user.getEmail())
			.nickname(user.getNickname())
			.birthDate(user.getBirthDate())
			.profileImage(user.getProfileImage())
			.active(user.isActive())
			.build();
	}

	public UserSummaryResponse toSummaryResponse(User user) {
		return UserSummaryResponse.builder()
			.id(user.getId())
			.fullName(user.getFullName())
			.nickname(user.getNickname())
			.profileImage(user.getProfileImage())
			.build();
	}

}
