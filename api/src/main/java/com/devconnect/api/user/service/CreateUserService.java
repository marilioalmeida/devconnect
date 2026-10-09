package com.devconnect.api.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.domain.Role;
import com.devconnect.api.user.mapper.UserMapper;
import com.devconnect.api.user.repository.UserRepository;
import com.devconnect.api.user.service.core.ValidateUniqueEmailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserService {

	private static final String DEFAULT_ROLE = "USER";

	private final UserRepository userRepository;
	private final ValidateUniqueEmailService validateUniqueEmailService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UserResponse create(UserRequest request) {
		validateUniqueEmailService.validate(request.getEmail());

		var user = UserMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));
		user.addRole(Role.builder().name(DEFAULT_ROLE).build());

		return UserMapper.toResponse(userRepository.save(user));
	}

}
