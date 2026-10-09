package com.devconnect.api.post.controller.response;

import java.time.LocalDateTime;

import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostResponse {

	private Long id;

	private String content;

	private LocalDateTime createdAt;

	private Visibility visibility;

	private UserSummaryResponse author;

	private long likeCount;

	private boolean likedByCurrentUser;

	private long commentCount;

}
