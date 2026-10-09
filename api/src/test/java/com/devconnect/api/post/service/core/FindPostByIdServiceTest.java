package com.devconnect.api.post.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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

import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class FindPostByIdServiceTest {

	@InjectMocks
	private FindPostByIdService tested;

	@Mock
	private PostRepository postRepository;

	@Test
	@DisplayName("Should return post when it exists")
	void shouldReturnPostWhenItExists() {

		Post post = PostFactory.getPublic();
		when(postRepository.findById(PostFactory.ID_POST)).thenReturn(Optional.of(post));

		Post result = tested.byId(PostFactory.ID_POST);

		verify(postRepository).findById(PostFactory.ID_POST);
		assertSame(post, result);
	}

	@Test
	@DisplayName("Should throw not found when post does not exist")
	void shouldThrowNotFoundWhenPostDoesNotExist() {

		when(postRepository.findById(PostFactory.ID_POST)).thenReturn(Optional.empty());

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.byId(PostFactory.ID_POST));

		assertEquals("Post not found", exception.getReason());
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}
}
