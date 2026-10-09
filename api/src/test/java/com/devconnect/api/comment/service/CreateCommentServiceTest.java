package com.devconnect.api.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.core.service.NowService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.CommentFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class CreateCommentServiceTest {

	@InjectMocks
	private CreateCommentService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindPostByIdService findPostByIdService;

	@Mock
	private ValidatePostAccessService validatePostAccessService;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private NowService nowService;

	@Captor
	private ArgumentCaptor<Comment> commentCaptor;

	@Test
	@DisplayName("Should create comment")
	void shouldCreateComment() {

		Post post = PostFactory.getPublic();
		User author = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(author);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(nowService.getDateTime()).thenReturn(CommentFactory.CREATED_AT);
		when(commentRepository.save(any(Comment.class)))
			.thenReturn(CommentFactory.getComment());

		tested.create(PostFactory.ID_POST, CommentFactory.getRequest());

		verify(commentRepository).save(commentCaptor.capture());

		Comment comment = commentCaptor.getValue();
		assertNull(comment.getId());
		assertSame(post, comment.getPost());
		assertSame(author, comment.getAuthor());
		assertEquals(CommentFactory.CONTENT, comment.getContent());
		assertEquals(CommentFactory.CREATED_AT, comment.getCreatedAt());
	}

	@Test
	@DisplayName("Should return saved comment")
	void shouldReturnSavedComment() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(nowService.getDateTime()).thenReturn(CommentFactory.CREATED_AT);
		when(commentRepository.save(any(Comment.class)))
			.thenReturn(CommentFactory.getComment());

		CommentResponse response = tested.create(PostFactory.ID_POST, CommentFactory.getRequest());

		assertEquals(CommentFactory.COMMENT_ID, response.getId());
		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getAuthor().getId());
	}

	@Test
	@DisplayName("Should validate access before commenting")
	void shouldValidateAccessBeforeCommenting() {

		Post post = PostFactory.getPublic();
		User author = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(author);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(nowService.getDateTime()).thenReturn(CommentFactory.CREATED_AT);
		when(commentRepository.save(any(Comment.class)))
			.thenReturn(CommentFactory.getComment());

		tested.create(PostFactory.ID_POST, CommentFactory.getRequest());

		verify(validatePostAccessService).validateAccess(post, author);
	}

	@Test
	@DisplayName("Should not comment when post does not exist")
	void shouldNotCommentWhenPostDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRecipient());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class,
				() -> tested.create(PostFactory.ID_POST, CommentFactory.getRequest()));

		verify(commentRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should not comment without post access")
	void shouldNotCommentWithoutPostAccess() {

		Post post = PostFactory.getPrivate();
		User thirdUser = FriendshipFactory.getThirdUser();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findPostByIdService.byId(PostFactory.PRIVATE_POST_ID)).thenReturn(post);
		doThrow(ResponseStatusException.class)
				.when(validatePostAccessService).validateAccess(post, thirdUser);

		assertThrows(ResponseStatusException.class,
				() -> tested.create(PostFactory.PRIVATE_POST_ID, CommentFactory.getRequest()));

		verify(commentRepository, never()).save(any());
	}
}
