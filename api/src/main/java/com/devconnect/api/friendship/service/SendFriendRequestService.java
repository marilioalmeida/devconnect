package com.devconnect.api.friendship.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.controller.request.FriendshipRequest;
import com.devconnect.api.friendship.controller.response.FriendshipResponse;
import com.devconnect.api.friendship.mapper.FriendshipMapper;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.friendship.service.core.ValidateNewFriendshipService;
import com.devconnect.api.core.service.NowService;
import com.devconnect.api.user.service.core.FindUserByIdService;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SendFriendRequestService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FindUserByIdService findUserByIdService;
	private final ValidateNewFriendshipService validateNewFriendshipService;
	private final FriendshipRepository friendshipRepository;
	private final NowService nowService;

	@Transactional
	public FriendshipResponse send(FriendshipRequest request) {
		var requester = authenticatedUserService.get();
		var recipient = findUserByIdService.byId(request.getRecipientId());

		validateNewFriendshipService.validate(requester, recipient);

		var friendship = FriendshipMapper.toEntity(requester, recipient, nowService.getDateTime());

		try {
			return FriendshipMapper.toResponse(friendshipRepository.saveAndFlush(friendship));
		} catch (DataIntegrityViolationException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"There is already a pending friend request with this user");
		}
	}

}
