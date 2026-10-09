package com.devconnect.api.friendship.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.friendship.controller.response.FriendRequestResponse;
import com.devconnect.api.friendship.mapper.FriendshipMapper;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListReceivedFriendRequestsService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FriendshipRepository friendshipRepository;

	@Transactional(readOnly = true)
	public List<FriendRequestResponse> list() {
		var user = authenticatedUserService.get();

		return friendshipRepository.findReceivedRequests(user.getId()).stream()
			.map(FriendshipMapper::toFriendRequestResponse)
			.toList();
	}

}
