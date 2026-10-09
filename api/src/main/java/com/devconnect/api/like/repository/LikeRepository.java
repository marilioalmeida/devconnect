package com.devconnect.api.like.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devconnect.api.like.domain.PostLike;
import com.devconnect.api.post.repository.PostCount;

public interface LikeRepository extends JpaRepository<PostLike, Long> {

	Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

	boolean existsByPostIdAndUserId(Long postId, Long userId);

	long countByPostId(Long postId);

	@Query("""
		select new com.devconnect.api.post.repository.PostCount(l.post.id, count(l.id))
		from PostLike l
		where l.post.id in :postIds
		group by l.post.id
		""")
	List<PostCount> countByPosts(@Param("postIds") Collection<Long> postIds);

	@Query("""
		select l.post.id
		from PostLike l
		where l.user.id = :userId
		  and l.post.id in :postIds
		""")
	List<Long> findPostIdsLikedBy(
		@Param("userId") Long userId,
		@Param("postIds") Collection<Long> postIds);

	@Query(value = """
		select l
		from PostLike l
		join fetch l.user
		where l.post.id = :postId
		  and l.user.active = true
		order by l.createdAt desc, l.id desc
		""",
		countQuery = """
		select count(l)
		from PostLike l
		where l.post.id = :postId
		  and l.user.active = true
		""")
	Page<PostLike> findByPost(@Param("postId") Long postId, Pageable pageable);

}
