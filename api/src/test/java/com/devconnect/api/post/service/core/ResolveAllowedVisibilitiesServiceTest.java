package com.devconnect.api.post.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devconnect.api.friendship.service.core.FindFriendshipBetweenService;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.post.domain.Visibility;

@ExtendWith(MockitoExtension.class)
class ResolveAllowedVisibilitiesServiceTest {

	@InjectMocks
	private ResolveAllowedVisibilitiesService tested;

	@Mock
	private FindFriendshipBetweenService findFriendshipBetweenService;

	@Test
	@DisplayName("Should allow all visibilities on own profile")
	void shouldAllowAllVisibilitiesOnOwnProfile() {

		var authenticated = FriendshipFactory.getRequester();

		List<Visibility> allowed = tested.allowed(authenticated, authenticated);

		assertEquals(2, allowed.size());
		assertTrue(allowed.contains(Visibility.PUBLIC));
		assertTrue(allowed.contains(Visibility.PRIVATE));
		verify(findFriendshipBetweenService, never()).between(any(), any());
	}

	@Test
	@DisplayName("Should allow all visibilities when friendship is accepted")
	void shouldAllowAllVisibilitiesWhenFriendshipIsAccepted() {

		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(FriendshipFactory.getAccepted()));

		List<Visibility> allowed =
				tested.allowed(FriendshipFactory.getRequester(), FriendshipFactory.getRecipient());

		assertEquals(2, allowed.size());
		assertTrue(allowed.contains(Visibility.PRIVATE));
	}

	@Test
	@DisplayName("Should allow only public when friendship is pending")
	void shouldAllowOnlyPublicWhenFriendshipIsPending() {

		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.RECIPIENT_ID))
			.thenReturn(Optional.of(FriendshipFactory.getPending()));

		List<Visibility> allowed =
				tested.allowed(FriendshipFactory.getRequester(), FriendshipFactory.getRecipient());

		assertEquals(List.of(Visibility.PUBLIC), allowed);
	}

	@Test
	@DisplayName("Should allow only public without relationship")
	void shouldAllowOnlyPublicWithoutRelationship() {

		when(findFriendshipBetweenService.between(FriendshipFactory.REQUESTER_ID, FriendshipFactory.THIRD_USER_ID))
			.thenReturn(Optional.empty());

		List<Visibility> allowed =
				tested.allowed(FriendshipFactory.getRequester(), FriendshipFactory.getThirdUser());

		assertEquals(List.of(Visibility.PUBLIC), allowed);
	}
}
