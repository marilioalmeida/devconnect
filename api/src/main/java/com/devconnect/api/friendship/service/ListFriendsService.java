package com.devconnect.api.friendship.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.friendship.controller.response.FriendResponse;
import com.devconnect.api.friendship.mapper.FriendshipMapper;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.core.pagination.Pagination;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListFriendsService {

	private final AuthenticatedUserService authenticatedUserService;
	private final FriendshipRepository friendshipRepository;

	@Transactional(readOnly = true)
	public Page<FriendResponse> list(String search, Pageable pageable) {
		var user = authenticatedUserService.get();

		return friendshipRepository.findFriends(user.getId(), normalize(search), Pagination.withoutSort(pageable))
			.map(friendship -> FriendshipMapper.toFriendResponse(friendship, user.getId()));
	}

	private String normalize(String search) {
		return search == null ? "" : search.trim();
	}


}
