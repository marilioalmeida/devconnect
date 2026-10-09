package com.devconnect.api.user.controller.response;

import com.devconnect.api.friendship.controller.response.RelationshipResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileResponse {

	private Long id;

	private String fullName;

	private String nickname;

	private String profileImage;

	private RelationshipResponse relationship;

}
