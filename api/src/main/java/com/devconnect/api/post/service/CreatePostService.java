package com.devconnect.api.post.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.core.service.NowService;
import com.devconnect.api.post.controller.request.PostRequest;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.mapper.PostMapper;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreatePostService {

	private final AuthenticatedUserService authenticatedUserService;
	private final PostRepository postRepository;
	private final NowService nowService;

	@Transactional
	public PostResponse create(PostRequest request) {
		var author = authenticatedUserService.get();

		var post = PostMapper.toEntity(request, author, nowService.getDateTime());

		return PostMapper.toResponse(postRepository.save(post));
	}

}
