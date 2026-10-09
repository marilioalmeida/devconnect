package com.devconnect.api.post.service.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;

@ExtendWith(MockitoExtension.class)
class ValidatePostAuthorshipServiceTest {

	@InjectMocks
	private ValidatePostAuthorshipService tested;

	@Test
	@DisplayName("Should not throw when user is author")
	void shouldNotThrowWhenUserIsAuthor() {

		Post post = PostFactory.getPublic();

		assertDoesNotThrow(() -> tested.validateAuthor(post, FriendshipFactory.getRequester()));
	}

	@Test
	@DisplayName("Should throw forbidden when user is not author")
	void shouldThrowForbiddenWhenUserIsNotAuthor() {

		Post post = PostFactory.getPublic();

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> tested.validateAuthor(post, FriendshipFactory.getThirdUser()));

		assertEquals("Only the author can change this post", exception.getReason());
		assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
	}
}
