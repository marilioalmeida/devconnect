package com.devconnect.api.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.mapper.PostMapper;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.post.service.core.ValidatePostAuthorshipService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class UpdatePostContentServiceTest {

	@InjectMocks
	private UpdatePostContentService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FindPostByIdService findPostByIdService;

	@Mock
	private ValidatePostAuthorshipService validatePostAuthorshipService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private EnrichPostsService enrichPostsService;

	@Test
	@DisplayName("Should update post content")
	void shouldUpdatePostContent() {

		Post post = PostFactory.getPublic();
		User authenticated = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(postRepository.save(post)).thenReturn(post);
		when(enrichPostsService.enrich(post, authenticated))
			.thenAnswer(invocation -> PostMapper.toResponse(post));

		PostResponse response = tested.update(
			PostFactory.ID_POST, PostFactory.getUpdateContentRequest("  Revised content  "));

		assertEquals("Revised content", post.getContent());
		verify(postRepository).save(post);
		assertEquals("Revised content", response.getContent());
	}

	@Test
	@DisplayName("Should validate authorship before updating")
	void shouldValidateAuthorshipBeforeUpdating() {

		Post post = PostFactory.getPublic();
		User authenticated = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(authenticated);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		when(postRepository.save(post)).thenReturn(post);

		tested.update(PostFactory.ID_POST, PostFactory.getUpdateContentRequest("New text"));

		verify(validatePostAuthorshipService).validateAuthor(post, authenticated);
	}

	@Test
	@DisplayName("Should not update when user is not author")
	void shouldNotUpdateWhenUserIsNotAuthor() {

		Post post = PostFactory.getPublic();
		User thirdUser = FriendshipFactory.getThirdUser();
		String originalContent = post.getContent();

		when(authenticatedUserService.get()).thenReturn(thirdUser);
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenReturn(post);
		doThrow(ResponseStatusException.class)
				.when(validatePostAuthorshipService).validateAuthor(post, thirdUser);

		assertThrows(ResponseStatusException.class, () -> tested.update(
			PostFactory.ID_POST, PostFactory.getUpdateContentRequest("New text")));

		assertEquals(originalContent, post.getContent());
		verify(postRepository, never()).save(any());
	}

	@Test
	@DisplayName("Should not update when post does not exist")
	void shouldNotUpdateWhenPostDoesNotExist() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(findPostByIdService.byId(PostFactory.ID_POST)).thenThrow(ResponseStatusException.class);

		assertThrows(ResponseStatusException.class, () -> tested.update(
			PostFactory.ID_POST, PostFactory.getUpdateContentRequest("New text")));

		verify(postRepository, never()).save(any());
	}
}
