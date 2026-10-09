package com.devconnect.api.post.service.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.domain.Visibility;

@ExtendWith(MockitoExtension.class)
class ValidatePostAccessServiceTest {

	private static final List<Visibility> ALL = List.of(Visibility.PUBLIC, Visibility.PRIVATE);
	private static final List<Visibility> PUBLIC_ONLY = List.of(Visibility.PUBLIC);

	@InjectMocks
	private ValidatePostAccessService tested;

	@Mock
	private ResolveAllowedVisibilitiesService resolveAllowedVisibilitiesService;

	@Test
	@DisplayName("Should allow access to a stranger's public post")
	void shouldAllowAccessToStrangerPublicPost() {

		Post post = PostFactory.getPublic();
		var thirdUser = FriendshipFactory.getThirdUser();

		when(resolveAllowedVisibilitiesService.allowed(thirdUser, post.getAuthor()))
			.thenReturn(PUBLIC_ONLY);

		assertDoesNotThrow(() -> tested.validateAccess(post, thirdUser));
	}

	@Test
	@DisplayName("Should allow author access to their own private post")
	void shouldAllowAuthorAccessToOwnPrivatePost() {

		Post post = PostFactory.getPrivate();
		var author = FriendshipFactory.getRequester();

		when(resolveAllowedVisibilitiesService.allowed(author, post.getAuthor()))
			.thenReturn(ALL);

		assertDoesNotThrow(() -> tested.validateAccess(post, author));
	}

	@Test
	@DisplayName("Should allow access to a friend's private post")
	void shouldAllowAccessToFriendPrivatePost() {

		Post post = PostFactory.getPrivate();
		var friend = FriendshipFactory.getRecipient();

		when(resolveAllowedVisibilitiesService.allowed(friend, post.getAuthor()))
			.thenReturn(ALL);

		assertDoesNotThrow(() -> tested.validateAccess(post, friend));
	}

	@Test
	@DisplayName("Should fail to access private post without friendship")
	void shouldFailToAccessPrivatePostWithoutFriendship() {

		Post post = PostFactory.getPrivate();
		var thirdUser = FriendshipFactory.getThirdUser();

		when(resolveAllowedVisibilitiesService.allowed(thirdUser, post.getAuthor()))
			.thenReturn(PUBLIC_ONLY);

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.validateAccess(post, thirdUser));

		assertEquals("You do not have access to this post", exception.getReason());
		assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
	}
}
