package com.devconnect.api.factories;

import java.time.LocalDateTime;

import com.devconnect.api.like.domain.PostLike;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.repository.PostCount;
import com.devconnect.api.user.domain.User;

public class LikeFactory {

	public static final Long LIKE_ID = 200L;
	public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 2, 6, 9, 15);

	public static PostLike.PostLikeBuilder getBuilder() {
		return PostLike.builder()
			.id(LIKE_ID)
			.post(PostFactory.getPublic())
			.user(FriendshipFactory.getRecipient())
			.createdAt(CREATED_AT);
	}

	public static PostLike getLike() {
		return getBuilder().build();
	}

	public static PostLike getBy(User user) {
		return getBuilder().user(user).build();
	}

	public static PostLike getOn(Post post, User user) {
		return getBuilder().post(post).user(user).build();
	}

	public static PostLike getNew() {
		return getBuilder().id(null).build();
	}

	public static PostCount getCount(Long postId, Long total) {
		return new PostCount(postId, total);
	}
}
