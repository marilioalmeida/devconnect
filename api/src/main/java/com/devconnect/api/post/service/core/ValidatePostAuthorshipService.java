package com.devconnect.api.post.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.post.domain.Post;
import com.devconnect.api.user.domain.User;

@Service
public class ValidatePostAuthorshipService {

	public void validateAuthor(Post post, User user) {
		if (!post.getAuthor().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can change this post");
		}
	}

}
