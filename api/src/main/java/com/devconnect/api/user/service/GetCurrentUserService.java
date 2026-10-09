package com.devconnect.api.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.mapper.UserMapper;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCurrentUserService {

	private final AuthenticatedUserService authenticatedUserService;

	@Transactional(readOnly = true)
	public UserResponse getCurrent() {
		return UserMapper.toResponse(authenticatedUserService.get());
	}

}
