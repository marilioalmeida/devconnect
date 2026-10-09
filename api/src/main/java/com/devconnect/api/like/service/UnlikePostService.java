package com.devconnect.api.like.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.mapper.LikeMapper;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnlikePostService {

	private final AuthenticatedUserService authenticatedUserService;
	private final LikeRepository likeRepository;

	@Transactional
	public LikeSummaryResponse unlike(Long postId) {
		var authenticated = authenticatedUserService.get();

		var like = likeRepository.findByPostIdAndUserId(postId, authenticated.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Like not found"));

		likeRepository.delete(like);

		return LikeMapper.toSummaryResponse(postId, likeRepository.countByPostId(postId), false);
	}

}
