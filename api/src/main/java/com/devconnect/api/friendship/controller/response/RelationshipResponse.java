package com.devconnect.api.friendship.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RelationshipResponse {

	private RelationshipStatus status;

	private Long friendshipId;

}
