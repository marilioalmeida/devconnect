package com.devconnect.api.friendship.controller.response;

import java.time.LocalDateTime;

import com.devconnect.api.user.controller.response.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FriendRequestResponse {

	private Long id;

	private LocalDateTime requestedAt;

	private UserSummaryResponse requester;

}
