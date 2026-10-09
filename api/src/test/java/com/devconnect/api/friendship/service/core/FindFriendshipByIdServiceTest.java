package com.devconnect.api.friendship.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.devconnect.api.friendship.domain.Friendship;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.factories.FriendshipFactory;

@ExtendWith(MockitoExtension.class)
class FindFriendshipByIdServiceTest {

	@InjectMocks
	private FindFriendshipByIdService tested;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Test
	@DisplayName("Should find friendship by ID")
	void shouldFindFriendshipById() {

		Friendship friendship = FriendshipFactory.getPending();
		when(friendshipRepository.findById(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(Optional.of(friendship));

		Friendship result = tested.byId(FriendshipFactory.FRIENDSHIP_ID);

		verify(friendshipRepository).findById(FriendshipFactory.FRIENDSHIP_ID);
		assertSame(friendship, result);
	}

	@Test
	@DisplayName("Should fail when friendship does not exist")
	void shouldFailWhenFriendshipDoesNotExist() {

		when(friendshipRepository.findById(FriendshipFactory.FRIENDSHIP_ID)).thenReturn(Optional.empty());

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> tested.byId(FriendshipFactory.FRIENDSHIP_ID));

		assertEquals("Friendship not found", exception.getReason());
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}
}
