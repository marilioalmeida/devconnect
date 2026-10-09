package com.devconnect.api.friendship.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFriendshipByIdService {

	private final FriendshipRepository friendshipRepository;

	public Friendship byId(Long id) {
		return friendshipRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found"));
	}

}
