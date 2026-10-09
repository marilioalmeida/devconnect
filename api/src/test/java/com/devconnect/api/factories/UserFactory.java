package com.devconnect.api.factories;

import java.time.LocalDate;

import com.devconnect.api.user.controller.request.UserRequest;
import com.devconnect.api.user.domain.Role;
import com.devconnect.api.user.domain.User;

public class UserFactory {

	public static User.UserBuilder getBuilder() {
		return User.builder()
			.id(SimpleFactory.getRandomLong())
			.fullName("Test User")
			.email("test@devconnect.com")
			.nickname("tester")
			.birthDate(LocalDate.of(1995, 3, 15))
			.password("$2a$10$testHash")
			.profileImage("https://cdn.devconnect.com/profile/test.png")
			.active(true);
	}

	public static User getActive() {
		User user = getBuilder().build();
		user.addRole(Role.builder().name("USER").build());
		return user;
	}

	public static User getInactive() {
		return getBuilder().active(false).build();
	}

	public static User getWithoutOptionalData() {
		return getBuilder()
			.nickname(null)
			.profileImage(null)
			.build();
	}

	public static UserRequest getSignupRequest() {
		UserRequest request = new UserRequest();
		request.setFullName("Test User");
		request.setEmail("test@devconnect.com");
		request.setNickname("tester");
		request.setBirthDate(LocalDate.of(1995, 3, 15));
		request.setPassword("securePassword123");
		request.setProfileImage("https://cdn.devconnect.com/profile/test.png");
		return request;
	}

	public static UserRequest getSignupRequestWithoutOptionalData() {
		UserRequest request = new UserRequest();
		request.setFullName("Test User");
		request.setEmail("test@devconnect.com");
		request.setBirthDate(LocalDate.of(1995, 3, 15));
		request.setPassword("securePassword123");
		return request;
	}
}
