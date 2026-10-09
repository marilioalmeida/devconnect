package com.devconnect.api.user.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.core.text.Texts;
import com.devconnect.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateUniqueEmailService {

	private final UserRepository userRepository;

	public void validate(String email) {
		if (userRepository.existsByEmail(Texts.email(email))) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
	}

}
