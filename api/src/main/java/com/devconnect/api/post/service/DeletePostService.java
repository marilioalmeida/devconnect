package com.devconnect.api.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.FindPostByIdService;
import com.devconnect.api.post.service.core.ValidatePostAuthorshipService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeletePostService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindPostByIdService findPostByIdService;
	private final ValidatePostAuthorshipService validatePostAuthorshipService;
	private final PostRepository postRepository;

	@Transactional
	public void delete(Long postId) {
		var authenticated = authenticatedUserService.get();
		var post = findPostByIdService.byId(postId);

		validatePostAuthorshipService.validateAuthor(post, authenticated);

		postRepository.delete(post);
	}

}
