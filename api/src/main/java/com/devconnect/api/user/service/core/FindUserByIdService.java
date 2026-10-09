package com.devconnect.api.user.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindUserByIdService {

	private final UserRepository userRepository;

	public User byId(Long id) {
		return userRepository.findById(id)
			.filter(User::isActive)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

}
