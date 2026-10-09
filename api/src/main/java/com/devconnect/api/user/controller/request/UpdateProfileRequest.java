package com.devconnect.api.user.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

	@NotBlank
	@Size(max = 255)
	private String fullName;

	@Size(max = 50)
	private String nickname;

	@Size(max = 512)
	private String profileImage;

}
