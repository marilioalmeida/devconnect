package com.devconnect.api.comment.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.comment.service.core.FindCommentByIdService;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCommentService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindCommentByIdService findCommentByIdService;
	private final CommentRepository commentRepository;

	@Transactional
	public void delete(Long postId, Long commentId) {
		var authenticated = authenticatedUserService.get();
		var comment = findCommentByIdService.byId(postId, commentId);

		validatePermission(comment, authenticated);

		commentRepository.delete(comment);
	}

	private void validatePermission(Comment comment, User authenticated) {
		boolean commentAuthor = comment.getAuthor().getId().equals(authenticated.getId());
		boolean postAuthor = comment.getPost().getAuthor().getId().equals(authenticated.getId());

		if (!commentAuthor && !postAuthor) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Only the comment author or the post author can delete this comment");
		}
	}

}
