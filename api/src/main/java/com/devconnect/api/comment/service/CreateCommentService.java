package com.devconnect.api.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.comment.controller.request.CommentRequest;
import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.mapper.CommentMapper;
import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.core.service.NowService;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAccessService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCommentService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAccessService validatePostAccessService;
	private final CommentRepository commentRepository;
	private final NowService nowService;

	@Transactional
	public CommentResponse create(Long postId, CommentRequest request) {
		var author = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAccessService.validateAccess(post, author);

		var comment = CommentMapper.toEntity(request, post, author, nowService.getDateTime());

		return CommentMapper.toResponse(commentRepository.save(comment));
	}

}
