package com.devconnect.api.friendship.controller.response;

import java.time.LocalDateTime;

import com.devconnect.api.user.controller.response.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FriendResponse {

	private Long id;

	private LocalDateTime friendsSince;

	private UserSummaryResponse friend;

}
