package com.devconnect.api.post.controller.request;

import com.devconnect.api.post.domain.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostVisibilityRequest {

	@NotNull
	private Visibility visibility;

}
