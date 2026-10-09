package com.devconnect.api.post.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidatePostAccessService {

	private final ResolveAllowedVisibilitiesService resolveAllowedVisibilitiesService;

	public void validateAccess(Post post, User user) {
		var allowed = resolveAllowedVisibilitiesService.allowed(user, post.getAuthor());

		if (!allowed.contains(post.getVisibility())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this post");
		}
	}

}
