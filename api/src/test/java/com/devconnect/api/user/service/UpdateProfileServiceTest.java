package com.devconnect.api.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.devconnect.api.user.controller.request.UpdateProfileRequest;
import com.devconnect.api.user.controller.response.UserResponse;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class UpdateProfileServiceTest {

	@InjectMocks
	private UpdateProfileService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("Should update name nickname and image")
	void shouldUpdateNameNicknameAndImage() {

		User user = UserFactory.getActive();
		UpdateProfileRequest request = UserFactory.getUpdateProfileRequest();

		when(authenticatedUserService.get()).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		UserResponse response = tested.update(request);

		assertEquals("Updated Name", user.getFullName());
		assertEquals("updated", user.getNickname());
		assertEquals("https://cdn.devconnect.com/profile/updated.png", user.getProfileImage());

		verify(userRepository).save(user);

		assertEquals("Updated Name", response.getFullName());
		assertEquals(user.getId(), response.getId());
	}

	@Test
	@DisplayName("Should not change email password birth date or active")
	void shouldNotChangeEmailPasswordBirthDateOrActive() {

		User user = UserFactory.getActive();
		String emailOriginal = user.getEmail();
		String originalPassword = user.getPassword();
		var originalBirthDate = user.getBirthDate();

		when(authenticatedUserService.get()).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		tested.update(UserFactory.getUpdateProfileRequest());

		assertEquals(emailOriginal, user.getEmail());
		assertEquals(originalPassword, user.getPassword());
		assertEquals(originalBirthDate, user.getBirthDate());
		assertTrue(user.isActive());
	}

	@Test
	@DisplayName("Should clear nickname and image when missing")
	void shouldClearNicknameAndImageWhenMissing() {

		User user = UserFactory.getActive();

		when(authenticatedUserService.get()).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		tested.update(UserFactory.getUpdateProfileRequestWithoutOptionalData());

		assertNull(user.getNickname());
		assertNull(user.getProfileImage());
	}

	@Test
	@DisplayName("Should treat blank fields as missing")
	void shouldTreatBlankFieldsAsMissing() {

		User user = UserFactory.getActive();
		UpdateProfileRequest request = UserFactory.getUpdateProfileRequest();
		request.setNickname("   ");
		request.setProfileImage("  ");

		when(authenticatedUserService.get()).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		tested.update(request);

		assertNull(user.getNickname());
		assertNull(user.getProfileImage());
	}

	@Test
	@DisplayName("Should trim name")
	void shouldTrimName() {

		User user = UserFactory.getActive();
		UpdateProfileRequest request = UserFactory.getUpdateProfileRequest();
		request.setFullName("  Name With Spaces  ");

		when(authenticatedUserService.get()).thenReturn(user);
		when(userRepository.save(user)).thenReturn(user);

		tested.update(request);

		assertEquals("Name With Spaces", user.getFullName());
	}

	@Test
	@DisplayName("Should not update without authenticated user")
	void shouldNotUpdateWithoutAuthenticatedUser() {

		when(authenticatedUserService.get()).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class,
				() -> tested.update(UserFactory.getUpdateProfileRequest()));

		verify(userRepository, never()).save(any());
	}
}
