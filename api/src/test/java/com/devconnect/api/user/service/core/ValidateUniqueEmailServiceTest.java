package com.devconnect.api.user.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ValidateUniqueEmailServiceTest {

	@InjectMocks
	private ValidateUniqueEmailService tested;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("Should do nothing when email is unique")
	void shouldDoNothingWhenEmailIsUnique() {

		String email = "new@devconnect.com";
		when(userRepository.existsByEmail(email)).thenReturn(false);

		tested.validate(email);

		verify(userRepository).existsByEmail(email);
	}

	@Test
	@DisplayName("Should fail when email is duplicated")
	void shouldFailWhenEmailIsDuplicated() {

		String email = "test@devconnect.com";
		when(userRepository.existsByEmail(email)).thenReturn(true);

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.validate(email));

		assertEquals("Email already registered", exception.getReason());
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}
}
