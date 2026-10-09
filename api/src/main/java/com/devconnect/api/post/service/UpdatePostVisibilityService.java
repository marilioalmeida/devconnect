package com.devconnect.api.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.post.controller.request.UpdatePostVisibilityRequest;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.post.service.core.ValidatePostAuthorshipService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePostVisibilityService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAuthorshipService validatePostAuthorshipService;
	private final PostRepository postRepository;
	private final EnrichPostsService enrichPostsService;

	@Transactional
	public PostResponse update(Long postId, UpdatePostVisibilityRequest request) {
		var authenticated = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAuthorshipService.validateAuthor(post, authenticated);

		post.setVisibility(request.getVisibility());

		return enrichPostsService.enrich(postRepository.save(post), authenticated);
	}

}
