package com.devconnect.api.friendship.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.friendship.mapper.FriendshipMapper;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.friendship.service.core.FindFriendshipByIdService;
import com.devconnect.api.friendship.service.core.ValidateFriendshipParticipantService;
import com.devconnect.api.core.service.NowService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcceptFriendshipService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindFriendshipByIdService findFriendshipByIdService;
	private final ValidateFriendshipParticipantService validateFriendshipParticipantService;
	private final FriendshipRepository friendshipRepository;
	private final NowService nowService;

	@Transactional
	public FriendshipResponse accept(Long friendshipId) {
		var user = authenticatedUserService.get();
		var friendship = findFriendshipByIdService.byId(friendshipId);

		validateFriendshipParticipantService.validateRecipient(friendship, user);
		validatePending(friendship);

		friendship.setStatus(FriendshipStatus.ACCEPTED);
		friendship.setRespondedAt(nowService.getDateTime());

		return FriendshipMapper.toResponse(friendshipRepository.save(friendship));
	}

	private void validatePending(Friendship friendship) {
		if (FriendshipStatus.ACCEPTED.equals(friendship.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "This friend request has already been accepted");
		}
	}

}
