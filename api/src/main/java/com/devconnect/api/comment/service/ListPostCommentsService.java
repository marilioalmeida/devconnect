package com.devconnect.api.comment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.mapper.CommentMapper;
import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.core.pagination.Pagination;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListPostCommentsService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAccessService validatePostAccessService;
	private final CommentRepository commentRepository;

	@Transactional(readOnly = true)
	public Page<CommentResponse> list(Long postId, Pageable pageable) {
		var authenticated = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAccessService.validateAccess(post, authenticated);

		return commentRepository.findByPost(post.getId(), Pagination.withoutSort(pageable))
			.map(CommentMapper::toResponse);
	}


}
