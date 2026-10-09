package com.devconnect.api.comment.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.factories.CommentFactory;
import com.devconnect.api.factories.PostFactory;

@ExtendWith(MockitoExtension.class)
class FindCommentByIdServiceTest {

	@InjectMocks
	private FindCommentByIdService tested;

	@Mock
	private CommentRepository commentRepository;

	@Test
	@DisplayName("Should find comment of post")
	void shouldFindCommentOfPost() {

		Comment comment = CommentFactory.getComment();
		when(commentRepository.findById(CommentFactory.COMMENT_ID))
				.thenReturn(Optional.of(comment));

		Comment result = tested.byId(PostFactory.ID_POST, CommentFactory.COMMENT_ID);

		assertSame(comment, result);
	}

	@Test
	@DisplayName("Should fail when comment belongs to another post")
	void shouldFailWhenCommentBelongsToAnotherPost() {

		Comment comment = CommentFactory.getComment();
		when(commentRepository.findById(CommentFactory.COMMENT_ID))
				.thenReturn(Optional.of(comment));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.byId(PostFactory.PRIVATE_POST_ID, CommentFactory.COMMENT_ID));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	@DisplayName("Should fail when comment does not exist")
	void shouldFailWhenCommentDoesNotExist() {

		when(commentRepository.findById(CommentFactory.COMMENT_ID))
				.thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.byId(PostFactory.ID_POST, CommentFactory.COMMENT_ID));

		assertEquals("Comment not found", exception.getReason());
	}
}
