package com.devconnect.api.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.friendship.service.core.FindFriendshipBetweenService;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.user.mapper.ProfileMapper;
import com.devconnect.api.user.service.core.FindUserByIdService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProfileService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindUserByIdService findUserByIdService;
	private final FindFriendshipBetweenService findFriendshipBetweenService;

	@Transactional(readOnly = true)
	public ProfileResponse get(Long userId) {
		var authenticated = authenticatedUserService.get();
		var user = findUserByIdService.byId(userId);

		var relationship = findFriendshipBetweenService.between(authenticated.getId(), user.getId()).orElse(null);

		return ProfileMapper.toResponse(user, relationship, authenticated.getId());
	}

}
