package com.devconnect.api.post.service.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.mapper.PostMapper;
import com.devconnect.api.post.repository.PostCount;
import com.devconnect.api.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EnrichPostsService {

	private final LikeRepository likeRepository;
	private final CommentRepository commentRepository;

	public Page<PostResponse> enrich(Page<Post> posts, User authenticated) {
		if (posts.isEmpty()) {
			return posts.map(PostMapper::toResponse);
		}

		return enrich(posts, posts.getContent(), authenticated);
	}

	public PostResponse enrich(Post post, User authenticated) {
		var ids = List.of(post.getId());

		return build(
			post,
			index(likeRepository.countByPosts(ids)),
			index(commentRepository.countByPosts(ids)),
			Set.copyOf(likeRepository.findPostIdsLikedBy(authenticated.getId(), ids)));
	}

	private Page<PostResponse> enrich(Page<Post> page, List<Post> posts, User authenticated) {
		var ids = posts.stream().map(Post::getId).toList();

		var likes = index(likeRepository.countByPosts(ids));
		var comments = index(commentRepository.countByPosts(ids));
		var likedByUser = Set.copyOf(
			likeRepository.findPostIdsLikedBy(authenticated.getId(), ids));

		return page.map(post -> build(post, likes, comments, likedByUser));
	}

	private Map<Long, Long> index(List<PostCount> counts) {
		return counts.stream()
			.collect(Collectors.toMap(PostCount::postId, PostCount::total));
	}

	private PostResponse build(
			Post post,
			Map<Long, Long> likes,
			Map<Long, Long> comments,
			Set<Long> likedByUser) {
		return PostMapper.toResponse(
			post,
			likes.getOrDefault(post.getId(), 0L),
			likedByUser.contains(post.getId()),
			comments.getOrDefault(post.getId(), 0L));
	}

}
