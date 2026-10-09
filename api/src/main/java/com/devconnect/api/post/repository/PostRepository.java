package com.devconnect.api.post.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devconnect.api.post.domain.Post;
import com.devconnect.api.post.domain.Visibility;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query(value = """
		select p
		from Post p
		join fetch p.author
		where p.author.active = true
		  and (p.author.id = :userId
		   or exists (
		        select f.id
		        from Friendship f
		        where f.status = com.devconnect.api.friendship.domain.FriendshipStatus.ACCEPTED
		          and ((f.requester.id = :userId and f.recipient.id = p.author.id)
		            or (f.recipient.id = :userId and f.requester.id = p.author.id))
		      ))
		order by p.createdAt desc, p.id desc
		""",
		countQuery = """
		select count(p)
		from Post p
		where p.author.active = true
		  and (p.author.id = :userId
		   or exists (
		        select f.id
		        from Friendship f
		        where f.status = com.devconnect.api.friendship.domain.FriendshipStatus.ACCEPTED
		          and ((f.requester.id = :userId and f.recipient.id = p.author.id)
		            or (f.recipient.id = :userId and f.requester.id = p.author.id))
		      ))
		""")
	Page<Post> findFeed(@Param("userId") Long userId, Pageable pageable);

	@Query(value = """
		select p
		from Post p
		join fetch p.author
		where p.author.id = :authorId
		  and p.visibility in :visibilities
		order by p.createdAt desc, p.id desc
		""",
		countQuery = """
		select count(p)
		from Post p
		where p.author.id = :authorId
		  and p.visibility in :visibilities
		""")
	Page<Post> findByAuthor(
		@Param("authorId") Long authorId,
		@Param("visibilities") Collection<Visibility> visibilities,
		Pageable pageable);

}
