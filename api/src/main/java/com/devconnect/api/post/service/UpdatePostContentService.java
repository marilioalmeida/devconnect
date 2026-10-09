package com.devconnect.api.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.core.text.Texts;
import com.devconnect.api.post.controller.request.UpdatePostContentRequest;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.post.service.core.ValidatePostAuthorshipService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePostContentService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAuthorshipService validatePostAuthorshipService;
	private final PostRepository postRepository;
	private final EnrichPostsService enrichPostsService;

	@Transactional
	public PostResponse update(Long postId, UpdatePostContentRequest request) {
		var authenticated = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAuthorshipService.validateAuthor(post, authenticated);

		post.setContent(Texts.required(request.getContent()));

		return enrichPostsService.enrich(postRepository.save(post), authenticated);
	}

}
