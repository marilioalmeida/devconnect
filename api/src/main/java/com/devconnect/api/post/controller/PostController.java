package com.devconnect.api.post.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devconnect.api.core.config.OpenApiConfig;
import com.devconnect.api.post.controller.request.UpdatePostContentRequest;
import com.devconnect.api.post.controller.request.UpdatePostVisibilityRequest;
import com.devconnect.api.post.controller.request.PostRequest;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.service.UpdatePostContentService;
import com.devconnect.api.post.service.UpdatePostVisibilityService;
import com.devconnect.api.post.service.CreatePostService;
import com.devconnect.api.post.service.ListFeedService;
import com.devconnect.api.post.service.DeletePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCOPE_USER')")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
@Tag(name = "Posts")
public class PostController {

	private final CreatePostService createPostService;
	private final UpdatePostContentService updatePostContentService;
	private final UpdatePostVisibilityService updatePostVisibilityService;
	private final DeletePostService deletePostService;
	private final ListFeedService listFeedService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Publish a new post")
	public PostResponse create(@RequestBody @Valid PostRequest request) {
		return createPostService.create(request);
	}

	@PatchMapping("/{postId}/visibility")
	@Operation(summary = "Change the visibility of your own post")
	public PostResponse updateVisibility(
			@PathVariable Long postId,
			@RequestBody @Valid UpdatePostVisibilityRequest request) {
		return updatePostVisibilityService.update(postId, request);
	}

	@PatchMapping("/{postId}")
	@Operation(summary = "Edit the content of your own post")
	public PostResponse updateContent(
			@PathVariable Long postId,
			@RequestBody @Valid UpdatePostContentRequest request) {
		return updatePostContentService.update(postId, request);
	}

	@DeleteMapping("/{postId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete your own post")
	public void delete(@PathVariable Long postId) {
		deletePostService.delete(postId);
	}

	@GetMapping("/feed")
	@Operation(summary = "List posts from the authenticated user and their friends")
	public Page<PostResponse> listFeed(Pageable pageable) {
		return listFeedService.list(pageable);
	}

}
