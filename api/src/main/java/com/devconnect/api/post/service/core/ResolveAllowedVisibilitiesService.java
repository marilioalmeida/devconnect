package com.devconnect.api.post.service.core;

import java.util.List;

import org.springframework.stereotype.Service;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.domain.FriendshipStatus;
import com.devconnect.api.friendship.service.core.FindFriendshipBetweenService;
import com.devconnect.api.post.domain.Visibility;
import com.devconnect.api.user.domain.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveAllowedVisibilitiesService {

	private static final List<Visibility> ALL = List.of(Visibility.PUBLIC, Visibility.PRIVATE);
	private static final List<Visibility> PUBLIC_ONLY = List.of(Visibility.PUBLIC);

	private final FindFriendshipBetweenService findFriendshipBetweenService;

	public List<Visibility> allowed(User authenticated, User profileOwner) {
		if (authenticated.getId().equals(profileOwner.getId())) {
			return ALL;
		}

		return findFriendshipBetweenService.between(authenticated.getId(), profileOwner.getId())
			.filter(this::accepted)
			.map(friendship -> ALL)
			.orElse(PUBLIC_ONLY);
	}

	private boolean accepted(Friendship friendship) {
		return FriendshipStatus.ACCEPTED.equals(friendship.getStatus());
	}

}
