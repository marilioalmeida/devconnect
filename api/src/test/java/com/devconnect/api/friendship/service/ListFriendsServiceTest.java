package com.devconnect.api.friendship.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.devconnect.api.friendship.controller.response.FriendResponse;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class ListFriendsServiceTest {

	private static final Pageable PAGEABLE = PageRequest.of(0, 20);

	@InjectMocks
	private ListFriendsService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Test
	@DisplayName("Should list friends")
	void shouldListFriends() {

		User requester = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(friendshipRepository.findFriends(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of(FriendshipFactory.getAccepted())));

		Page<FriendResponse> response = tested.list(null, PAGEABLE);

		assertEquals(1, response.getContent().size());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, response.getContent().get(0).getId());
		assertEquals(FriendshipFactory.RECIPIENT_ID, response.getContent().get(0).getFriend().getId());
	}

	@Test
	@DisplayName("Should resolve friend when authenticated is recipient")
	void shouldResolveFriendWhenAuthenticatedIsRecipient() {

		User recipient = FriendshipFactory.getRecipient();

		when(authenticatedUserService.get()).thenReturn(recipient);
		when(friendshipRepository.findFriends(FriendshipFactory.RECIPIENT_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of(FriendshipFactory.getAccepted())));

		Page<FriendResponse> response = tested.list(null, PAGEABLE);

		assertEquals(FriendshipFactory.REQUESTER_ID, response.getContent().get(0).getFriend().getId());
	}

	@Test
	@DisplayName("Should normalize null search to empty text")
	void shouldNormalizeNullSearchToEmptyText() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(friendshipRepository.findFriends(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.list(null, PAGEABLE);

		verify(friendshipRepository).findFriends(FriendshipFactory.REQUESTER_ID, "", PAGEABLE);
	}

	@Test
	@DisplayName("Should trim given search")
	void shouldTrimGivenSearch() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(friendshipRepository.findFriends(FriendshipFactory.REQUESTER_ID, "ana", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.list("  ana ", PAGEABLE);

		verify(friendshipRepository).findFriends(FriendshipFactory.REQUESTER_ID, "ana", PAGEABLE);
	}

	@Test
	@DisplayName("Should ignore client sort")
	void shouldIgnoreClientSort() {

		Pageable withSort = PageRequest.of(0, 20, Sort.by("fullName"));

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(friendshipRepository.findFriends(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.list(null, withSort);

		verify(friendshipRepository).findFriends(FriendshipFactory.REQUESTER_ID, "", PAGEABLE);
	}

	@Test
	@DisplayName("Should return empty page without friends")
	void shouldReturnEmptyPageWithoutFriends() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(friendshipRepository.findFriends(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		Page<FriendResponse> response = tested.list(null, PAGEABLE);

		assertTrue(response.getContent().isEmpty());
	}
}
