package com.devconnect.api.post.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
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

import com.devconnect.api.core.service.NowService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class CreatePostServiceTest {

	@InjectMocks
	private CreatePostService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private PostRepository postRepository;

	@Mock
	private NowService nowService;

	@Captor
	private ArgumentCaptor<Post> postCaptor;

	@Test
	@DisplayName("Should create post for authenticated user")
	void shouldCreatePostForAuthenticatedUser() {

		User author = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(author);
		when(nowService.getDateTime()).thenReturn(PostFactory.CREATED_AT);
		when(postRepository.save(any(Post.class))).thenReturn(PostFactory.getPublic());

		tested.create(PostFactory.getRequest());

		verify(postRepository).save(postCaptor.capture());

		Post post = postCaptor.getValue();
		assertNull(post.getId());
		assertSame(author, post.getAuthor());
		assertEquals(PostFactory.CONTENT, post.getContent());
		assertEquals(PostFactory.CREATED_AT, post.getCreatedAt());
	}

	@Test
	@DisplayName("Should create post with given visibility")
	void shouldCreatePostWithGivenVisibility() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(nowService.getDateTime()).thenReturn(PostFactory.CREATED_AT);
		when(postRepository.save(any(Post.class))).thenReturn(PostFactory.getPrivate());

		tested.create(PostFactory.getRequest(Visibility.PRIVATE));

		verify(postRepository).save(postCaptor.capture());

		assertEquals(Visibility.PRIVATE, postCaptor.getValue().getVisibility());
	}

	@Test
	@DisplayName("Should return saved post in response")
	void shouldReturnSavedPostInResponse() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(nowService.getDateTime()).thenReturn(PostFactory.CREATED_AT);
		when(postRepository.save(any(Post.class))).thenReturn(PostFactory.getPublic());

		PostResponse response = tested.create(PostFactory.getRequest());

		assertEquals(PostFactory.ID_POST, response.getId());
		assertEquals(FriendshipFactory.REQUESTER_ID, response.getAuthor().getId());
	}
}
