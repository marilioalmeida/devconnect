package com.devconnect.api.comment.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devconnect.api.comment.controller.request.CommentRequest;
import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.CommentFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;

class CommentMapperTest {

	@Test
	@DisplayName("Should map request to entity")
	void shouldMapRequestToEntity() {

		Post post = PostFactory.getPublic();
		User author = FriendshipFactory.getRecipient();

		Comment comment = CommentMapper.toEntity(
			CommentFactory.getRequest(), post, author, CommentFactory.CREATED_AT);

		assertNull(comment.getId());
		assertSame(post, comment.getPost());
		assertSame(author, comment.getAuthor());
		assertEquals(CommentFactory.CONTENT, comment.getContent());
		assertEquals(CommentFactory.CREATED_AT, comment.getCreatedAt());
	}

	@Test
	@DisplayName("Should trim content")
	void shouldTrimContent() {

		CommentRequest request = CommentFactory.getRequest("   Comment with spaces   ");

		Comment comment = CommentMapper.toEntity(
			request, PostFactory.getPublic(), FriendshipFactory.getRecipient(),
			CommentFactory.CREATED_AT);

		assertEquals("Comment with spaces", comment.getContent());
	}

	@Test
	@DisplayName("Should map comment to response")
	void shouldMapCommentToResponse() {

		CommentResponse response = CommentMapper.toResponse(CommentFactory.getComment());

		assertEquals(CommentFactory.COMMENT_ID, response.getId());
		assertEquals(CommentFactory.CONTENT, response.getContent());
		assertEquals(CommentFactory.CREATED_AT, response.getCreatedAt());
	}

	@Test
	@DisplayName("Should map nested author")
	void shouldMapNestedAuthor() {

		CommentResponse response = CommentMapper.toResponse(CommentFactory.getComment());

		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getAuthor().getId());
		assertEquals("Bruno Recipient", response.getAuthor().getFullName());
	}
}
