package com.devconnect.api.comment.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindCommentByIdService {

	private final CommentRepository commentRepository;

	public Comment byId(Long postId, Long commentId) {
		return commentRepository.findById(commentId)
			.filter(comment -> comment.getPost().getId().equals(postId))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
	}

}
