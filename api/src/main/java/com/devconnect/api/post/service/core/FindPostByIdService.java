package com.devconnect.api.post.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindPostByIdService {

	private final PostRepository postRepository;

	public Post byId(Long id) {
		return postRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
	}

}
