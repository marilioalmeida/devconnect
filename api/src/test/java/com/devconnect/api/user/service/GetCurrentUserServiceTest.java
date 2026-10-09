package com.devconnect.api.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.UserFactory;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserServiceTest {

	@InjectMocks
	private GetCurrentUserService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Test
	@DisplayName("Should get authenticated user")
	void shouldGetAuthenticatedUser() {

		User user = UserFactory.getActive();
		when(authenticatedUserService.get()).thenReturn(user);

		UserResponse response = tested.getCurrent();

		verify(authenticatedUserService).get();
		assertEquals(user.getId(), response.getId());
		assertEquals(user.getFullName(), response.getFullName());
		assertEquals(user.getEmail(), response.getEmail());
		assertTrue(response.isActive());
	}

	@Test
	@DisplayName("Should not get user when not authenticated")
	void shouldNotGetUserWhenNotAuthenticated() {

		when(authenticatedUserService.get()).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.getCurrent());
	}
}
