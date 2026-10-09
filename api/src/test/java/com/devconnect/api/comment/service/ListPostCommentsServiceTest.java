package com.devconnect.api.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.CommentFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ListPostCommentsServiceTest {

	private static final Pageable PAGEABLE = PageRequest.of(0, 20);

	@InjectMocks
	private ListPostCommentsService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindPostByIdService findPostByIdService;

	@Mock
	private ValidatePostAccessService validatePostAccessService;

	@Mock
	private CommentRepository commentRepository;

	@Test
	@DisplayName("Should list the comments of a post")
	void shouldListPostComments() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(commentRepository.findByPost(PostFactory.ID_POST, PAGEABLE))
			.thenReturn(new PageImpl<>(List.of(CommentFactory.getComment())));

		Page<CommentResponse> response = tested.list(PostFactory.ID_POST, PAGEABLE);

		assertEquals(1, response.getContent().size());
		assertEquals(CommentFactory.COMMENT_ID, response.getContent().get(0).getId());
		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getContent().get(0).getAuthor().getId());
	}

	@Test
	@DisplayName("Should ignore sort requested by client")
	void shouldIgnoreSortRequestedByClient() {

		Pageable withSort = PageRequest.of(0, 20, Sort.by("createdAt"));

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(commentRepository.findByPost(PostFactory.ID_POST, PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.list(PostFactory.ID_POST, withSort);

		verify(commentRepository).findByPost(PostFactory.ID_POST, PAGEABLE);
	}

	@Test
	@DisplayName("Should return empty page without comments")
	void shouldReturnEmptyPageWithoutComments() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(PostFactory.getPublic());
		when(commentRepository.findByPost(PostFactory.ID_POST, PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		Page<CommentResponse> response = tested.list(PostFactory.ID_POST, PAGEABLE);

		assertTrue(response.getContent().isEmpty());
	}

	@Test
	@DisplayName("Should not list when post does not exist")
	void shouldNotListWhenPostDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.list(PostFactory.ID_POST, PAGEABLE));

		verify(commentRepository, never()).findByPost(anyLong(), any());
	}

	@Test
	@DisplayName("Should not list without post access")
	void shouldNotListWithoutPostAccess() {

		Post post = PostFactory.getPrivate();
		User thirdUser = FriendshipFactory.getThirdUser();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findPostByIdService.byId(PostFactory.PRIVATE_POST_ID)).thenReturn(post);
		doThrow(ResponseStatusException.class)
				.when(validatePostAccessService).validateAccess(post, thirdUser);

		assertThrows(ResponseStatusException.class,
				() -> tested.list(PostFactory.PRIVATE_POST_ID, PAGEABLE));

		verify(commentRepository, never()).findByPost(anyLong(), any());
	}
}
