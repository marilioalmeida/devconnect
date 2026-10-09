package com.devconnect.api.like.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LikeSummaryResponse {

	private Long postId;

	private long likeCount;

	private boolean likedByCurrentUser;

}
