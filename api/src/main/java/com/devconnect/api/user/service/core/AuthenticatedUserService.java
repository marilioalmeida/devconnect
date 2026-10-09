package com.devconnect.api.user.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {

	private final UserRepository userRepository;

	public User get() {
		return userRepository.findByEmailAndActiveTrue(currentEmail())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
	}

	private String currentEmail() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Request without a valid token");
		}

		return jwt.getClaimAsString("email");
	}

}
