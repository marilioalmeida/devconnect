package com.devconnect.api.friendship.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.friendship.service.core.FindFriendshipByIdService;
import com.devconnect.api.friendship.service.core.ValidateFriendshipParticipantService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteFriendshipService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindFriendshipByIdService findFriendshipByIdService;
	private final ValidateFriendshipParticipantService validateFriendshipParticipantService;
	private final FriendshipRepository friendshipRepository;

	@Transactional
	public void delete(Long friendshipId) {
		var user = authenticatedUserService.get();
		var friendship = findFriendshipByIdService.byId(friendshipId);

		validateFriendshipParticipantService.validateParticipant(friendship, user);

		friendshipRepository.delete(friendship);
	}

}
