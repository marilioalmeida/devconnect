package com.devconnect.api.friendship.controller.response;

import java.time.LocalDateTime;

import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FriendshipResponse {

	private Long id;

	private FriendshipStatus status;

	private LocalDateTime requestedAt;

	private LocalDateTime respondedAt;

	private UserSummaryResponse requester;

	private UserSummaryResponse recipient;

}
