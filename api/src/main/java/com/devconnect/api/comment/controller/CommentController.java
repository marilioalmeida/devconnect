package com.devconnect.api.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.comment.controller.request.CommentRequest;
import com.devconnect.api.comment.controller.response.CommentResponse;
import com.devconnect.api.comment.service.CreateCommentService;
import com.devconnect.api.comment.service.ListPostCommentsService;
import com.devconnect.api.comment.service.DeleteCommentService;
import com.devconnect.api.core.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_USER')")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
@Tag(name = "Comments")
public class CommentController {

	private final CreateCommentService createCommentService;
	private final ListPostCommentsService listPostCommentsService;
	private final DeleteCommentService deleteCommentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Comment on a post")
	public CommentResponse create(
			@PathVariable Long postId,
			@RequestBody @Valid CommentRequest request) {
		return createCommentService.create(postId, request);
	}

	@DeleteMapping("/{commentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a comment as its author or as the post author")
	public void delete(@PathVariable Long postId, @PathVariable Long commentId) {
		deleteCommentService.delete(postId, commentId);
	}

	@GetMapping
	@Operation(summary = "List the comments of a post")
	public Page<CommentResponse> list(@PathVariable Long postId, Pageable pageable) {
		return listPostCommentsService.list(postId, pageable);
	}

}
