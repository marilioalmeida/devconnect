package com.devconnect.api.user.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.UserFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserServiceTest {

	@InjectMocks
	private AuthenticatedUserService tested;

	@Mock
	private UserRepository userRepository;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Should get authenticated user entity")
	void shouldGetAuthenticatedUserEntity() {

		User user = UserFactory.getActive();
		authenticateWithToken(user.getEmail());

		when(userRepository.findByEmailAndActiveTrue(user.getEmail())).thenReturn(Optional.of(user));

		User result = tested.get();

		verify(userRepository).findByEmailAndActiveTrue(user.getEmail());
		assertEquals(user, result);
	}

	@Test
	@DisplayName("Should fail when token user does not exist")
	void shouldFailWhenTokenUserDoesNotExist() {

		String email = "missing@devconnect.com";
		authenticateWithToken(email);

		when(userRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.empty());

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.get());

		assertEquals("Authenticated user not found", exception.getReason());
	}

	@Test
	@DisplayName("Should not get user without authentication")
	void shouldNotGetUserWithoutAuthentication() {

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.get());

		assertEquals("Request without a valid token", exception.getReason());
		verify(userRepository, never()).findByEmailAndActiveTrue(any());
	}

	@Test
	@DisplayName("Should not get user when principal is not JWT")
	void shouldNotGetUserWhenPrincipalIsNotJwt() {

		SecurityContextHolder.getContext().setAuthentication(
				UsernamePasswordAuthenticationToken.unauthenticated("test@devconnect.com", "password"));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.get());

		assertEquals("Request without a valid token", exception.getReason());
		verify(userRepository, never()).findByEmailAndActiveTrue(any());
	}

	private void authenticateWithToken(String email) {
		Jwt jwt = Jwt.withTokenValue("test-token")
			.header("alg", "none")
			.claim("email", email)
			.build();

		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
	}
}
