package com.devconnect.api.user.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.UserFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FindUserByIdServiceTest {

	@InjectMocks
	private FindUserByIdService tested;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("Should find user by ID")
	void shouldFindUserById() {

		User user = UserFactory.getActive();
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		User result = tested.byId(user.getId());

		verify(userRepository).findById(user.getId());
		assertSame(user, result);
	}

	@Test
	@DisplayName("Should fail when user is inactive")
	void shouldFailWhenUserIsInactive() {

		User user = UserFactory.getInactive();
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.byId(user.getId()));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	@DisplayName("Should fail when user does not exist")
	void shouldFailWhenUserDoesNotExist() {

		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.byId(1L));

		assertEquals("User not found", exception.getReason());
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}
}
