package com.devconnect.api.post.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.controller.request.PostRequest;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.user.domain.User;

class PostMapperTest {

	@Test
	@DisplayName("Should map request to entity")
	void shouldMapRequestToEntity() {

		User author = FriendshipFactory.getRequester();
		PostRequest request = PostFactory.getRequest(Visibility.PRIVATE);

		Post post = PostMapper.toEntity(request, author, PostFactory.CREATED_AT);

		assertNull(post.getId());
		assertSame(author, post.getAuthor());
		assertEquals(PostFactory.CONTENT, post.getContent());
		assertEquals(PostFactory.CREATED_AT, post.getCreatedAt());
		assertEquals(Visibility.PRIVATE, post.getVisibility());
	}

	@Test
	@DisplayName("Should trim content when mapping")
	void shouldTrimContentWhenMapping() {

		PostRequest request = PostFactory.getRequest();
		request.setContent("   Content with spaces   ");

		Post post = PostMapper.toEntity(request, FriendshipFactory.getRequester(), PostFactory.CREATED_AT);

		assertEquals("Content with spaces", post.getContent());
	}

	@Test
	@DisplayName("Should map post to response with all fields")
	void shouldMapPostToResponseWithAllFields() {

		PostResponse response = PostMapper.toResponse(PostFactory.getPublic());

		assertEquals(PostFactory.ID_POST, response.getId());
		assertEquals(PostFactory.CONTENT, response.getContent());
		assertEquals(PostFactory.CREATED_AT, response.getCreatedAt());
		assertEquals(Visibility.PUBLIC, response.getVisibility());
	}

	@Test
	@DisplayName("Should map nested author in response")
	void shouldMapNestedAuthorInResponse() {

		PostResponse response = PostMapper.toResponse(PostFactory.getPublic());

		assertEquals(FriendshipFactory.REQUESTER_ID, response.getAuthor().getId());
		assertEquals("Ana Requester", response.getAuthor().getFullName());
		assertEquals("ana", response.getAuthor().getNickname());
	}

	@Test
	@DisplayName("Should keep private visibility in response")
	void shouldKeepPrivateVisibilityInResponse() {

		PostResponse response = PostMapper.toResponse(PostFactory.getPrivate());

		assertEquals(Visibility.PRIVATE, response.getVisibility());
		assertEquals(PostFactory.PRIVATE_POST_ID, response.getId());
	}

	@Test
	@DisplayName("Should map with given interactions")
	void shouldMapWithGivenInteractions() {

		PostResponse response = PostMapper.toResponse(PostFactory.getPublic(), 7L, true, 3L);

		assertEquals(7L, response.getLikeCount());
		assertTrue(response.isLikedByCurrentUser());
		assertEquals(3L, response.getCommentCount());
	}

	@Test
	@DisplayName("Should default interactions to zero when missing")
	void shouldDefaultInteractionsToZeroWhenMissing() {

		PostResponse response = PostMapper.toResponse(PostFactory.getPublic());

		assertEquals(0L, response.getLikeCount());
		assertFalse(response.isLikedByCurrentUser());
		assertEquals(0L, response.getCommentCount());
	}
}
