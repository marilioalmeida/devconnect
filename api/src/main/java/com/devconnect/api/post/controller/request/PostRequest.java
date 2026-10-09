package com.devconnect.api.post.controller.request;

import com.devconnect.api.post.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequest {

	@NotBlank
	@Size(max = 5000)
	private String content;

	@NotNull
	private Visibility visibility;

}
