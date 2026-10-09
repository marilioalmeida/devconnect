package com.devconnect.api.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.UserFactory;
import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.domain.Role;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;
import com.devconnect.api.user.service.core.ValidateUniqueEmailService;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

	private static final String ENCODED_PASSWORD = "$2a$10$testHash";

	@InjectMocks
	private CreateUserService tested;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ValidateUniqueEmailService validateUniqueEmailService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Captor
	private ArgumentCaptor<User> userCaptor;

	@Test
	@DisplayName("Should create user")
	void shouldCreateUser() {

		UserRequest request = UserFactory.getSignupRequest();
		User savedUser = UserFactory.getActive();

		when(passwordEncoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		UserResponse response = tested.create(request);

		verify(validateUniqueEmailService).validate(request.getEmail());
		verify(passwordEncoder).encode(request.getPassword());
		verify(userRepository).save(userCaptor.capture());

		User user = userCaptor.getValue();
		assertEquals(request.getFullName(), user.getFullName());
		assertEquals(request.getEmail(), user.getEmail());
		assertEquals(request.getBirthDate(), user.getBirthDate());
		assertEquals(ENCODED_PASSWORD, user.getPassword());
		assertNotEquals(request.getPassword(), user.getPassword());
		assertTrue(user.isActive());

		assertEquals(savedUser.getId(), response.getId());
	}

	@Test
	@DisplayName("Should create user with default role")
	void shouldCreateUserWithDefaultRole() {

		UserRequest request = UserFactory.getSignupRequest();

		when(passwordEncoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);
		when(userRepository.save(any(User.class))).thenReturn(UserFactory.getActive());

		tested.create(request);

		verify(userRepository).save(userCaptor.capture());

		User user = userCaptor.getValue();
		assertEquals(1, user.getRoles().size());

		Role role = user.getRoles().get(0);
		assertEquals("USER", role.getName());
		assertSame(user, role.getUser());
	}

	@Test
	@DisplayName("Should not create user with duplicated email")
	void shouldNotCreateUserWithDuplicatedEmail() {

		UserRequest request = UserFactory.getSignupRequest();

		doThrow(ResponseStatusException.class)
				.when(validateUniqueEmailService).validate(request.getEmail());

		assertThrows(ResponseStatusException.class, () -> tested.create(request));

		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
	}
}
