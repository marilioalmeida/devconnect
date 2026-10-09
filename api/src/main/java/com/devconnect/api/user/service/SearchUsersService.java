package com.devconnect.api.user.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.core.pagination.Pagination;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.mapper.ProfileMapper;
import com.devconnect.api.user.repository.UserRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchUsersService {

	private final AuthenticatedUserService authenticatedUserService;
	private final UserRepository userRepository;
	private final FriendshipRepository friendshipRepository;

	@Transactional(readOnly = true)
	public Page<ProfileResponse> search(String search, Pageable pageable) {
		var authenticated = authenticatedUserService.get();

		var matches = userRepository.search(
			authenticated.getId(), normalize(search), Pagination.withoutSort(pageable));

		var relationships = matches.isEmpty()
			? Map.<Long, Friendship>of()
			: indexRelationships(authenticated.getId(), matches.getContent());

		return matches.map(user ->
			ProfileMapper.toResponse(user, relationships.get(user.getId()), authenticated.getId()));
	}

	private Map<Long, Friendship> indexRelationships(Long authenticatedId, List<User> users) {
		var ids = users.stream().map(User::getId).toList();

		return friendshipRepository.findAllBetween(authenticatedId, ids).stream()
			.collect(Collectors.toMap(
				friendship -> otherParticipantId(friendship, authenticatedId),
				Function.identity()));
	}

	private Long otherParticipantId(Friendship friendship, Long authenticatedId) {
		return friendship.getRequester().getId().equals(authenticatedId)
			? friendship.getRecipient().getId()
			: friendship.getRequester().getId();
	}

	private String normalize(String search) {
		return search == null ? "" : search.trim();
	}


}
