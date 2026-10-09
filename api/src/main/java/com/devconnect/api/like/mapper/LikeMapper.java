package com.devconnect.api.like.mapper;

import java.time.LocalDateTime;

import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.domain.PostLike;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LikeMapper {

	public PostLike toEntity(Post post, User user, LocalDateTime createdAt) {
		return PostLike.builder()
			.post(post)
			.user(user)
			.createdAt(createdAt)
			.build();
	}

	public LikeSummaryResponse toSummaryResponse(Long postId, long likeCount, boolean liked) {
		return LikeSummaryResponse.builder()
			.postId(postId)
			.likeCount(likeCount)
			.likedByCurrentUser(liked)
			.build();
	}

}
