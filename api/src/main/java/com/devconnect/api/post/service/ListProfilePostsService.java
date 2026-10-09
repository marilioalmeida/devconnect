package com.devconnect.api.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.core.pagination.Pagination;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.repository.PostRepository;
import com.devconnect.api.post.service.core.EnrichPostsService;
import com.devconnect.api.post.service.core.ResolveAllowedVisibilitiesService;
import com.devconnect.api.user.service.core.FindUserByIdService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListProfilePostsService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindUserByIdService findUserByIdService;
	private final ResolveAllowedVisibilitiesService resolveAllowedVisibilitiesService;
	private final PostRepository postRepository;
	private final EnrichPostsService enrichPostsService;

	@Transactional(readOnly = true)
	public Page<PostResponse> list(Long userId, Pageable pageable) {
		var authenticated = authenticatedUserService.get();
		var profileOwner = findUserByIdService.byId(userId);

		var visibilities = resolveAllowedVisibilitiesService.allowed(authenticated, profileOwner);

		var posts = postRepository.findByAuthor(
			profileOwner.getId(), visibilities, Pagination.withoutSort(pageable));

		return enrichPostsService.enrich(posts, authenticated);
	}


}
