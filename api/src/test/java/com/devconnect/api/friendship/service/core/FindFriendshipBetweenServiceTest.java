package com.devconnect.api.friendship.service.core;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.factories.FriendshipFactory;

@ExtendWith(MockitoExtension.class)
class FindFriendshipBetweenServiceTest {

	@InjectMocks
	private FindFriendshipBetweenService tested;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Test
	@DisplayName("Should find friendship between two users")
	void shouldFindFriendshipBetweenTwoUsers() {

		Friendship friendship = FriendshipFactory.getAccepted();
		when(friendshipRepository.findBetween(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(friendship));

		Optional<Friendship> result = tested.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID);

		verify(friendshipRepository).findBetween(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID);
		assertSame(friendship, result.orElseThrow());
	}

	@Test
	@DisplayName("Should return empty when there is no relationship")
	void shouldReturnEmptyWhenThereIsNoRelationship() {

		when(friendshipRepository.findBetween(FriendshipFactory.REQUESTER_ID, FriendshipFactory.THIRD_USER_ID))
			.thenReturn(Optional.empty());

		Optional<Friendship> result = tested.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.THIRD_USER_ID);

		assertTrue(result.isEmpty());
	}
}
