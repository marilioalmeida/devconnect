package com.devconnect.api.post.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAuthorshipService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class DeletePostServiceTest {

	@InjectMocks
	private DeletePostService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindPostByIdService findPostByIdService;

	@Mock
	private ValidatePostAuthorshipService validatePostAuthorshipService;

	@Mock
	private PostRepository postRepository;

	@Test
	@DisplayName("Should delete their own post")
	void shouldDeleteOwnPost() {

		Post post = PostFactory.getPublic();

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);

		tested.delete(PostFactory.ID_POST);

		verify(postRepository).delete(post);
	}

	@Test
	@DisplayName("Should validate authorship before deleting")
	void shouldValidateAuthorshipBeforeDeleting() {

		Post post = PostFactory.getPublic();
		User authenticated = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);

		tested.delete(PostFactory.ID_POST);

		verify(validatePostAuthorshipService).validateAuthor(post, authenticated);
	}

	@Test
	@DisplayName("Should not delete when user is not author")
	void shouldNotDeleteWhenUserIsNotAuthor() {

		Post post = PostFactory.getPublic();
		User thirdUser = FriendshipFactory.getThirdUser();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		doThrow(ResponseStatusException.class)
				.when(validatePostAuthorshipService).validateAuthor(post, thirdUser);

		assertThrows(ResponseStatusException.class, () -> tested.delete(PostFactory.ID_POST));

		verify(postRepository, never()).delete(any());
	}

	@Test
	@DisplayName("Should not delete when post does not exist")
	void shouldNotDeleteWhenPostDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.delete(PostFactory.ID_POST));

		verify(postRepository, never()).delete(any());
	}
}
