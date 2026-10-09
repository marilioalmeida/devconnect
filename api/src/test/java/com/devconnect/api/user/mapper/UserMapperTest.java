package com.devconnect.api.user.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devconnect.api.factories.UserFactory;
import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import com.devconnect.api.user.domain.User;

class UserMapperTest {

	private static final String ENCODED_PASSWORD = "$2a$10$testHash";

	@Test
	@DisplayName("Should map request to active entity")
	void shouldMapRequestToActiveEntity() {

		UserRequest request = UserFactory.getSignupRequest();

		User user = UserMapper.toEntity(request, ENCODED_PASSWORD);

		assertNotNull(user);
		assertNull(user.getId());
		assertEquals(request.getFullName(), user.getFullName());
		assertEquals(request.getEmail(), user.getEmail());
		assertEquals(request.getNickname(), user.getNickname());
		assertEquals(request.getBirthDate(), user.getBirthDate());
		assertEquals(request.getProfileImage(), user.getProfileImage());
		assertEquals(ENCODED_PASSWORD, user.getPassword());
		assertNotEquals(request.getPassword(), user.getPassword());
		assertTrue(user.isActive());
	}

	@Test
	@DisplayName("Should map request without optional data")
	void shouldMapRequestWithoutOptionalData() {

		UserRequest request = UserFactory.getSignupRequestWithoutOptionalData();

		User user = UserMapper.toEntity(request, ENCODED_PASSWORD);

		assertNull(user.getNickname());
		assertNull(user.getProfileImage());
		assertNotNull(user.getEmail());
	}

	@Test
	@DisplayName("Should start entity without roles")
	void shouldStartEntityWithoutRoles() {

		UserRequest request = UserFactory.getSignupRequest();

		User user = UserMapper.toEntity(request, ENCODED_PASSWORD);

		assertNotNull(user.getRoles());
		assertEquals(0, user.getRoles().size());
	}

	@Test
	@DisplayName("Should map active entity to response")
	void shouldMapActiveEntityToResponse() {

		User user = UserFactory.getActive();

		UserResponse response = UserMapper.toResponse(user);

		assertNotNull(response);
		assertEquals(user.getId(), response.getId());
		assertEquals(user.getFullName(), response.getFullName());
		assertEquals(user.getEmail(), response.getEmail());
		assertEquals(user.getNickname(), response.getNickname());
		assertEquals(user.getBirthDate(), response.getBirthDate());
		assertEquals(user.getProfileImage(), response.getProfileImage());
		assertTrue(response.isActive());
	}

	@Test
	@DisplayName("Should map inactive entity to response")
	void shouldMapInactiveEntityToResponse() {

		User user = UserFactory.getInactive();

		UserResponse response = UserMapper.toResponse(user);

		assertFalse(response.isActive());
		assertEquals(user.getId(), response.getId());
	}

	@Test
	@DisplayName("Should map entity without optional data to response")
	void shouldMapEntityWithoutOptionalDataToResponse() {

		User user = UserFactory.getWithoutOptionalData();

		UserResponse response = UserMapper.toResponse(user);

		assertNull(response.getNickname());
		assertNull(response.getProfileImage());
		assertNotNull(response.getId());
	}

	@Test
	@DisplayName("Should map entity to summary response")
	void shouldMapEntityToSummaryResponse() {

		User user = UserFactory.getActive();

		UserSummaryResponse response = UserMapper.toSummaryResponse(user);

		assertNotNull(response);
		assertEquals(user.getId(), response.getId());
		assertEquals(user.getFullName(), response.getFullName());
		assertEquals(user.getNickname(), response.getNickname());
		assertEquals(user.getProfileImage(), response.getProfileImage());
	}

	@Test
	@DisplayName("Should map entity without optional data to summary response")
	void shouldMapEntityWithoutOptionalDataToSummaryResponse() {

		User user = UserFactory.getWithoutOptionalData();

		UserSummaryResponse response = UserMapper.toSummaryResponse(user);

		assertNull(response.getNickname());
		assertNull(response.getProfileImage());
		assertNotNull(response.getId());
		assertEquals(user.getFullName(), response.getFullName());
	}
}
