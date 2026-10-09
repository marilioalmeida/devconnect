package com.devconnect.api.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.core.text.Texts;
import com.devconnect.api.user.controller.request.UpdateProfileRequest;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.mapper.UserMapper;
import com.devconnect.api.user.repository.UserRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProfileService {

	private final AuthenticatedUserService authenticatedUserService;
	private final UserRepository userRepository;

	@Transactional
	public UserResponse update(UpdateProfileRequest request) {
		var user = authenticatedUserService.get();

		user.setFullName(Texts.required(request.getFullName()));
		user.setNickname(Texts.optional(request.getNickname()));
		user.setProfileImage(Texts.optional(request.getProfileImage()));

		return UserMapper.toResponse(userRepository.save(user));
	}

}
