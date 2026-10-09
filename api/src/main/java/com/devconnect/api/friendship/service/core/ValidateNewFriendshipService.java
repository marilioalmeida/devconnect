package com.devconnect.api.friendship.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateNewFriendshipService {

	private final FindFriendshipBetweenService findFriendshipBetweenService;

	public void validate(User requester, User recipient) {
		if (requester.getId().equals(recipient.getId())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
					"You cannot send a friend request to yourself");
		}

		findFriendshipBetweenService.between(requester.getId(), recipient.getId())
			.ifPresent(this::rejectExistingRelationship);
	}

	private void rejectExistingRelationship(Friendship friendship) {
		if (FriendshipStatus.ACCEPTED.equals(friendship.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already friends");
		}

		throw new ResponseStatusException(HttpStatus.CONFLICT,
				"There is already a pending friend request with this user");
	}

}
