package com.devconnect.api.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.devconnect.api.comment.service.core.FindCommentByIdService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.CommentFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class DeleteCommentServiceTest {

	@InjectMocks
	private DeleteCommentService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindCommentByIdService findCommentByIdService;

	@Mock
	private CommentRepository commentRepository;

	@Test
	@DisplayName("Should allow comment author to delete")
	void shouldAllowCommentAuthorToDelete() {

		Comment comment = CommentFactory.getComment();

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findCommentByIdService.byId(PostFactory.ID_POST, CommentFactory.COMMENT_ID))
				.thenReturn(comment);

		tested.delete(PostFactory.ID_POST, CommentFactory.COMMENT_ID);

		verify(commentRepository).delete(comment);
	}

	@Test
	@DisplayName("Should allow post author to delete")
	void shouldAllowPostAuthorToDelete() {

		Comment comment = CommentFactory.getComment();

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findCommentByIdService.byId(PostFactory.ID_POST, CommentFactory.COMMENT_ID))
				.thenReturn(comment);

		tested.delete(PostFactory.ID_POST, CommentFactory.COMMENT_ID);

		verify(commentRepository).delete(comment);
	}

	@Test
	@DisplayName("Should not allow third user to delete")
	void shouldNotAllowThirdUserToDelete() {

		Comment comment = CommentFactory.getComment();

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getThirdUser());
		when(findCommentByIdService.byId(PostFactory.ID_POST, CommentFactory.COMMENT_ID))
				.thenReturn(comment);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.delete(PostFactory.ID_POST, CommentFactory.COMMENT_ID));

		assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
		verify(commentRepository, never()).delete(any());
	}

	@Test
	@DisplayName("Should not delete when comment does not exist")
	void shouldNotDeleteWhenCommentDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findCommentByIdService.byId(PostFactory.ID_POST, CommentFactory.COMMENT_ID))
				.thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class,
				() -> tested.delete(PostFactory.ID_POST, CommentFactory.COMMENT_ID));

		verify(commentRepository, never()).delete(any());
	}
}
