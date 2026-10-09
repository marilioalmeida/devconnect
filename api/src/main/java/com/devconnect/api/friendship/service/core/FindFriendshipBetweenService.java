package com.devconnect.api.friendship.service.core;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFriendshipBetweenService {

	private final FriendshipRepository friendshipRepository;

	public Optional<Friendship> between(Long userId, Long otherUserId) {
		return friendshipRepository.findBetween(userId, otherUserId);
	}

}
