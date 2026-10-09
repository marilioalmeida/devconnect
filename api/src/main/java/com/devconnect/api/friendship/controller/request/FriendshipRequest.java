package com.devconnect.api.friendship.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendshipRequest {

	@NotNull
	private Long recipientId;

}
