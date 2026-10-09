package com.devconnect.api.comment.controller.response;

import java.time.LocalDateTime;

import com.devconnect.api.user.controller.response.UserSummaryResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentResponse {

	private Long id;

	private String content;

	private LocalDateTime createdAt;

	private UserSummaryResponse author;

}
