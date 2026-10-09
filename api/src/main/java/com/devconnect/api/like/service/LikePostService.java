package com.devconnect.api.like.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.core.service.NowService;
import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.mapper.LikeMapper;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikePostService {

	private static final String ALREADY_LIKED = "You have already liked this post";

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAccessService validatePostAccessService;
	private final LikeRepository likeRepository;
	private final NowService nowService;

	@Transactional
	public LikeSummaryResponse like(Long postId) {
		var authenticated = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAccessService.validateAccess(post, authenticated);

		if (likeRepository.existsByPostIdAndUserId(post.getId(), authenticated.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ALREADY_LIKED);
		}

		save(post, authenticated);

		return LikeMapper.toSummaryResponse(
			post.getId(), likeRepository.countByPostId(post.getId()), true);
	}

	private void save(Post post, User authenticated) {
		try {
			likeRepository.saveAndFlush(
				LikeMapper.toEntity(post, authenticated, nowService.getDateTime()));
		} catch (DataIntegrityViolationException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, ALREADY_LIKED);
		}
	}

}
