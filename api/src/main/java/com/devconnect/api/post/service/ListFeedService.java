package com.devconnect.api.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.core.pagination.Pagination;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListFeedService {

	private final AuthenticatedUserService authenticatedUserService;
	private final PostRepository postRepository;
	private final EnrichPostsService enrichPostsService;

	@Transactional(readOnly = true)
	public Page<PostResponse> list(Pageable pageable) {
		var authenticated = authenticatedUserService.get();

		var posts = postRepository.findFeed(authenticated.getId(), Pagination.withoutSort(pageable));

		return enrichPostsService.enrich(posts, authenticated);
	}


}
