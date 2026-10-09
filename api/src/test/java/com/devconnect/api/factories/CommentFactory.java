package com.devconnect.api.factories;

import java.time.LocalDateTime;

import com.devconnect.api.comment.controller.request.CommentRequest;
import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.user.domain.User;

public class CommentFactory {

	public static final Long COMMENT_ID = 300L;
	public static final String CONTENT = "Congrats on the deploy! What stack did you use?";
	public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 2, 6, 10, 0);

	public static Comment.CommentBuilder getBuilder() {
		return Comment.builder()
			.id(COMMENT_ID)
			.post(PostFactory.getPublic())
			.author(FriendshipFactory.getRecipient())
			.content(CONTENT)
			.createdAt(CREATED_AT);
	}

	public static Comment getComment() {
		return getBuilder().build();
	}

	public static Comment getBy(User author) {
		return getBuilder().author(author).build();
	}

	public static Comment getNew() {
		return getBuilder().id(null).build();
	}

	public static CommentRequest getRequest() {
		return getRequest(CONTENT);
	}

	public static CommentRequest getRequest(String content) {
		CommentRequest request = new CommentRequest();
		request.setContent(content);
		return request;
	}
}
