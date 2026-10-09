package com.devconnect.api.user.controller.response;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {

	private Long id;

	private String fullName;

	private String email;

	private String nickname;

	private LocalDate birthDate;

	private String profileImage;

	private boolean active;

}
