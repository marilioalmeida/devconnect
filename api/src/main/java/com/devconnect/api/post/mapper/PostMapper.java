package com.devconnect.api.post.mapper;

import java.time.LocalDateTime;

import com.devconnect.api.post.controller.request.PostRequest;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.mapper.UserMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PostMapper {

	public Post toEntity(PostRequest request, User author, LocalDateTime createdAt) {
		return Post.builder()
			.author(author)
			.content(request.getContent().trim())
			.createdAt(createdAt)
			.visibility(request.getVisibility())
			.build();
	}

	public PostResponse toResponse(Post post) {
		return toResponse(post, 0L, false, 0L);
	}

	public PostResponse toResponse(
			Post post,
			long likeCount,
			boolean likedByCurrentUser,
			long commentCount) {
		return PostResponse.builder()
			.id(post.getId())
			.content(post.getContent())
			.createdAt(post.getCreatedAt())
			.visibility(post.getVisibility())
			.author(UserMapper.toSummaryResponse(post.getAuthor()))
			.likeCount(likeCount)
			.likedByCurrentUser(likedByCurrentUser)
			.commentCount(commentCount)
			.build();
	}

}
