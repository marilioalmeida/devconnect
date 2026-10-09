package com.devconnect.api.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.core.config.OpenApiConfig;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.service.ListProfilePostsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/{userId}/posts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_USER')")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
@Tag(name = "Posts")
public class UserPostsController {

	private final ListProfilePostsService listProfilePostsService;

	@GetMapping
	@Operation(summary = "List a user's posts respecting visibility")
	public Page<PostResponse> list(@PathVariable Long userId, Pageable pageable) {
		return listProfilePostsService.list(userId, pageable);
	}

}
