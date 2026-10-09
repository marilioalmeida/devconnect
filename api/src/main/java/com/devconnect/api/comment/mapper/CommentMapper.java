package com.devconnect.api.comment.mapper;

import java.time.LocalDateTime;

import com.devconnect.api.comment.controller.request.CommentRequest;
import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.mapper.UserMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommentMapper {

	public Comment toEntity(
			CommentRequest request,
			Post post,
			User author,
			LocalDateTime createdAt) {
		return Comment.builder()
			.post(post)
			.author(author)
			.content(request.getContent().trim())
			.createdAt(createdAt)
			.build();
	}

	public CommentResponse toResponse(Comment comment) {
		return CommentResponse.builder()
			.id(comment.getId())
			.content(comment.getContent())
			.createdAt(comment.getCreatedAt())
			.author(UserMapper.toSummaryResponse(comment.getAuthor()))
			.build();
	}

}
