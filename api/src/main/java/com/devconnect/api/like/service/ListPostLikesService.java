package com.devconnect.api.like.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.core.pagination.Pagination;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import com.devconnect.api.user.mapper.UserMapper;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListPostLikesService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAccessService validatePostAccessService;
	private final LikeRepository likeRepository;

	@Transactional(readOnly = true)
	public Page<UserSummaryResponse> list(Long postId, Pageable pageable) {
		var authenticated = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAccessService.validateAccess(post, authenticated);

		return likeRepository.findByPost(post.getId(), Pagination.withoutSort(pageable))
			.map(like -> UserMapper.toSummaryResponse(like.getUser()));
	}

}
