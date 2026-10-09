package com.devconnect.api.post.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostContentRequest {

	@NotBlank
	@Size(max = 5000)
	private String content;

}
