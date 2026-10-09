package com.devconnect.api.like.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.core.config.OpenApiConfig;
import com.devconnect.api.like.controller.response.LikeSummaryResponse;
import com.devconnect.api.like.service.LikePostService;
import com.devconnect.api.like.service.UnlikePostService;
import com.devconnect.api.like.service.ListPostLikesService;
import com.devconnect.api.user.controller.response.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_USER')")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
@Tag(name = "Likes")
public class LikeController {

	private final LikePostService likePostService;
	private final UnlikePostService unlikePostService;
	private final ListPostLikesService listPostLikesService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Like a post")
	public LikeSummaryResponse like(@PathVariable Long postId) {
		return likePostService.like(postId);
	}

	@GetMapping
	@Operation(summary = "List who liked a post")
	public Page<UserSummaryResponse> list(@PathVariable Long postId, Pageable pageable) {
		return listPostLikesService.list(postId, pageable);
	}

	@DeleteMapping
	@Operation(summary = "Remove your like from a post")
	public LikeSummaryResponse unlike(@PathVariable Long postId) {
		return unlikePostService.unlike(postId);
	}

}
