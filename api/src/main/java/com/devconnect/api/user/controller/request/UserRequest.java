package com.devconnect.api.user.controller.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

	@NotBlank
	@Size(max = 255)
	private String fullName;

	@NotBlank
	@Email
	@Size(max = 255)
	private String email;

	@Size(max = 50)
	private String nickname;

	@NotNull
	@Past
	private LocalDate birthDate;

	@NotBlank
	@Size(min = 8, max = 128)
	private String password;

	@Size(max = 512)
	private String profileImage;

}
