package com.devconnect.api.user.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSummaryResponse {

	private Long id;

	private String fullName;

	private String nickname;

	private String profileImage;

}
