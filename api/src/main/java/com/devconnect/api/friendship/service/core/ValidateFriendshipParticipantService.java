package com.devconnect.api.friendship.service.core;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.user.domain.User;

@Service
public class ValidateFriendshipParticipantService {

	public void validateParticipant(Friendship friendship, User user) {
		boolean participates = friendship.getRequester().getId().equals(user.getId())
				|| friendship.getRecipient().getId().equals(user.getId());

		if (!participates) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not part of this friendship");
		}
	}

	public void validateRecipient(Friendship friendship, User user) {
		if (!friendship.getRecipient().getId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Only the recipient can accept the friend request");
		}
	}

}
