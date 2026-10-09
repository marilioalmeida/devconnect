package com.devconnect.api.like.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.domain.PostLike;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.LikeFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;

class LikeMapperTest {

	@Test
	@DisplayName("Should map to entity")
	void shouldMapToEntity() {

		Post post = PostFactory.getPublic();
		User user = FriendshipFactory.getRecipient();

		PostLike like = LikeMapper.toEntity(post, user, LikeFactory.CREATED_AT);

		assertNull(like.getId());
		assertSame(post, like.getPost());
		assertSame(user, like.getUser());
		assertEquals(LikeFactory.CREATED_AT, like.getCreatedAt());
	}

	@Test
	@DisplayName("Should map summary of liked post")
	void shouldMapSummaryOfLikedPost() {

		LikeSummaryResponse response =
				LikeMapper.toSummaryResponse(PostFactory.ID_POST, 4L, true);

		assertEquals(PostFactory.ID_POST, response.getPostId());
		assertEquals(4L, response.getLikeCount());
		assertTrue(response.isLikedByCurrentUser());
	}

	@Test
	@DisplayName("Should map summary of post not liked")
	void shouldMapSummaryOfPostNotLiked() {

		LikeSummaryResponse response =
				LikeMapper.toSummaryResponse(PostFactory.ID_POST, 0L, false);

		assertEquals(0L, response.getLikeCount());
		assertFalse(response.isLikedByCurrentUser());
	}
}
