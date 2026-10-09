package com.devconnect.api.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.devconnect.api.friendship.controller.response.RelationshipStatus;
import com.devconnect.api.friendship.repository.FriendshipRepository;
import com.devconnect.api.user.controller.response.ProfileResponse;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.user.domain.User;
import com.devconnect.api.user.repository.UserRepository;
import com.devconnect.api.user.service.core.AuthenticatedUserService;

@ExtendWith(MockitoExtension.class)
class SearchUsersServiceTest {

	private static final Pageable PAGEABLE = PageRequest.of(0, 20);
	private static final List<Long> PAGE_IDS =
			List.of(FriendshipFactory.RECIPIENT_ID, FriendshipFactory.THIRD_USER_ID);

	@InjectMocks
	private SearchUsersService tested;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private FriendshipRepository friendshipRepository;

	@Test
	@DisplayName("Should search users with each relationship")
	void shouldSearchUsersWithEachRelationship() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(pageWithRecipientAndThirdUser());
		when(friendshipRepository.findAllBetween(FriendshipFactory.REQUESTER_ID, PAGE_IDS))
			.thenReturn(List.of(FriendshipFactory.getAccepted()));

		Page<ProfileResponse> response = tested.search(null, PAGEABLE);

		assertEquals(2, response.getContent().size());

		ProfileResponse withRelationship = response.getContent().get(0);
		assertEquals(FriendshipFactory.RECIPIENT_ID, withRelationship.getId());
		assertEquals(RelationshipStatus.FRIENDS, withRelationship.getRelationship().getStatus());
		assertEquals(FriendshipFactory.FRIENDSHIP_ID, withRelationship.getRelationship().getFriendshipId());

		ProfileResponse noRelationship = response.getContent().get(1);
		assertEquals(FriendshipFactory.THIRD_USER_ID, noRelationship.getId());
		assertEquals(RelationshipStatus.NONE, noRelationship.getRelationship().getStatus());
		assertNull(noRelationship.getRelationship().getFriendshipId());
	}

	@Test
	@DisplayName("Should load relationships in single call")
	void shouldLoadRelationshipsInSingleCall() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(pageWithRecipientAndThirdUser());
		when(friendshipRepository.findAllBetween(FriendshipFactory.REQUESTER_ID, PAGE_IDS))
			.thenReturn(List.of());

		tested.search(null, PAGEABLE);

		verify(friendshipRepository, times(1)).findAllBetween(FriendshipFactory.REQUESTER_ID, PAGE_IDS);
	}

	@Test
	@DisplayName("Should identify sent and received requests")
	void shouldIdentifySentAndReceivedRequests() {

		var requester = FriendshipFactory.getRequester();

		when(authenticatedUserService.get()).thenReturn(requester);
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(pageWithRecipientAndThirdUser());
		when(friendshipRepository.findAllBetween(FriendshipFactory.REQUESTER_ID, PAGE_IDS))
			.thenReturn(List.of(
				FriendshipFactory.getPending(),
				FriendshipFactory.getPendingBetween(FriendshipFactory.getThirdUser(), requester)));

		Page<ProfileResponse> response = tested.search(null, PAGEABLE);

		assertEquals(RelationshipStatus.REQUEST_SENT, response.getContent().get(0).getRelationship().getStatus());
		assertEquals(RelationshipStatus.REQUEST_RECEIVED, response.getContent().get(1).getRelationship().getStatus());
	}

	@Test
	@DisplayName("Should not load relationships without results")
	void shouldNotLoadRelationshipsWithoutResults() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		Page<ProfileResponse> response = tested.search(null, PAGEABLE);

		assertTrue(response.getContent().isEmpty());
		verify(friendshipRepository, never()).findAllBetween(anyLong(), any());
	}

	@Test
	@DisplayName("Should normalize null search to empty text")
	void shouldNormalizeNullSearchToEmptyText() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.search(null, PAGEABLE);

		verify(userRepository).search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE);
	}

	@Test
	@DisplayName("Should trim given search")
	void shouldTrimGivenSearch() {

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "bruno", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.search("  bruno ", PAGEABLE);

		verify(userRepository).search(FriendshipFactory.REQUESTER_ID, "bruno", PAGEABLE);
	}

	@Test
	@DisplayName("Should ignore client sort")
	void shouldIgnoreClientSort() {

		Pageable withSort = PageRequest.of(0, 20, Sort.by("fullName"));

		when(authenticatedUserService.get()).thenReturn(FriendshipFactory.getRequester());
		when(userRepository.search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE))
			.thenReturn(new PageImpl<>(List.of()));

		tested.search(null, withSort);

		verify(userRepository).search(FriendshipFactory.REQUESTER_ID, "", PAGEABLE);
	}

	private Page<User> pageWithRecipientAndThirdUser() {
		return new PageImpl<>(List.of(FriendshipFactory.getRecipient(), FriendshipFactory.getThirdUser()));
	}
}
