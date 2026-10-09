package com.devconnect.api.post.service.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import com.devconnect.api.comment.repository.CommentRepository;
import com.devconnect.api.like.repository.LikeRepository;
import com.devconnect.api.factories.FriendshipFactory;
import com.devconnect.api.factories.LikeFactory;
import com.devconnect.api.factories.PostFactory;
import com.devconnect.api.post.controller.response.PostResponse;
import com.devconnect.api.post.domain.Post;

@ExtendWith(MockitoExtension.class)
class EnrichPostsServiceTest {

	private static final List<Long> PAGE_IDS =
			List.of(PostFactory.ID_POST, PostFactory.PRIVATE_POST_ID);

	@InjectMocks
	private EnrichPostsService tested;

	@Mock
	private LikeRepository likeRepository;

	@Mock
	private CommentRepository commentRepository;

	@Test
	@DisplayName("Should enrich each post with counts and user like")
	void shouldEnrichEachPostWithCountsAndUserLike() {

		var authenticated = FriendshipFactory.getRecipient();

		when(likeRepository.countByPosts(PAGE_IDS))
			.thenReturn(List.of(LikeFactory.getCount(PostFactory.ID_POST, 3L)));
		when(commentRepository.countByPosts(PAGE_IDS))
			.thenReturn(List.of(LikeFactory.getCount(PostFactory.ID_POST, 1L)));
		when(likeRepository.findPostIdsLikedBy(FriendshipFactory.RECIPIENT_ID, PAGE_IDS))
			.thenReturn(List.of(PostFactory.ID_POST));

		Page<PostResponse> response = tested.enrich(page(), authenticated);

		PostResponse withInteractions = response.getContent().get(0);
		assertEquals(PostFactory.ID_POST, withInteractions.getId());
		assertEquals(3L, withInteractions.getLikeCount());
		assertEquals(1L, withInteractions.getCommentCount());
		assertTrue(withInteractions.isLikedByCurrentUser());
	}

	@Test
	@DisplayName("Should load page interactions with one call per query")
	void shouldLoadPageInteractionsWithOneCallPerQuery() {

		when(likeRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(commentRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(likeRepository.findPostIdsLikedBy(FriendshipFactory.RECIPIENT_ID, PAGE_IDS))
			.thenReturn(List.of());

		tested.enrich(page(), FriendshipFactory.getRecipient());

		verify(likeRepository, times(1)).countByPosts(PAGE_IDS);
		verify(commentRepository, times(1)).countByPosts(PAGE_IDS);
		verify(likeRepository, times(1))
			.findPostIdsLikedBy(FriendshipFactory.RECIPIENT_ID, PAGE_IDS);
	}

	@Test
	@DisplayName("Should return zero for post without likes or comments")
	void shouldReturnZeroForPostWithoutLikesOrComments() {

		when(likeRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(commentRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(likeRepository.findPostIdsLikedBy(FriendshipFactory.RECIPIENT_ID, PAGE_IDS))
			.thenReturn(List.of());

		Page<PostResponse> response = tested.enrich(page(), FriendshipFactory.getRecipient());

		PostResponse first = response.getContent().get(0);
		assertEquals(0L, first.getLikeCount());
		assertEquals(0L, first.getCommentCount());
		assertFalse(first.isLikedByCurrentUser());
	}

	@Test
	@DisplayName("Should not load interactions when page is empty")
	void shouldNotLoadInteractionsWhenPageIsEmpty() {

		Page<PostResponse> response =
				tested.enrich(new PageImpl<Post>(List.of()), FriendshipFactory.getRecipient());

		assertTrue(response.getContent().isEmpty());
		verify(likeRepository, never()).countByPosts(any());
		verify(commentRepository, never()).countByPosts(any());
		verify(likeRepository, never()).findPostIdsLikedBy(anyLong(), any());
	}

	@Test
	@DisplayName("Should mark as not liked when another user liked")
	void shouldMarkAsNotLikedWhenAnotherUserLiked() {

		when(likeRepository.countByPosts(PAGE_IDS))
			.thenReturn(List.of(LikeFactory.getCount(PostFactory.ID_POST, 1L)));
		when(commentRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(likeRepository.findPostIdsLikedBy(FriendshipFactory.THIRD_USER_ID, PAGE_IDS))
			.thenReturn(List.of());

		Page<PostResponse> response = tested.enrich(page(), FriendshipFactory.getThirdUser());

		PostResponse first = response.getContent().get(0);
		assertEquals(1L, first.getLikeCount());
		assertFalse(first.isLikedByCurrentUser());
	}

	@Test
	@DisplayName("Should preserve page metadata when enriching")
	void shouldPreservePageMetadataWhenEnriching() {

		Page<Post> page = new PageImpl<>(
			List.of(PostFactory.getPublic(), PostFactory.getPrivate()), PageRequest.of(0, 20), 42);

		when(likeRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(commentRepository.countByPosts(PAGE_IDS)).thenReturn(List.of());
		when(likeRepository.findPostIdsLikedBy(FriendshipFactory.RECIPIENT_ID, PAGE_IDS))
			.thenReturn(List.of());

		Page<PostResponse> response = tested.enrich(page, FriendshipFactory.getRecipient());

		assertEquals(42, response.getTotalElements());
		assertEquals(0, response.getNumber());
		assertEquals(20, response.getSize());
	}

	@Test
	@DisplayName("Should enrich single post")
	void shouldEnrichSinglePost() {

		var ids = List.of(PostFactory.ID_POST);

		when(likeRepository.countByPosts(ids))
			.thenReturn(List.of(LikeFactory.getCount(PostFactory.ID_POST, 5L)));
		when(commentRepository.countByPosts(ids))
			.thenReturn(List.of(LikeFactory.getCount(PostFactory.ID_POST, 2L)));
		when(likeRepository.findPostIdsLikedBy(FriendshipFactory.RECIPIENT_ID, ids))
			.thenReturn(List.of(PostFactory.ID_POST));

		PostResponse response =
				tested.enrich(PostFactory.getPublic(), FriendshipFactory.getRecipient());

		assertEquals(5L, response.getLikeCount());
		assertEquals(2L, response.getCommentCount());
		assertTrue(response.isLikedByCurrentUser());
	}

	private Page<Post> page() {
		return new PageImpl<>(List.of(PostFactory.getPublic(), PostFactory.getPrivate()));
	}
}
