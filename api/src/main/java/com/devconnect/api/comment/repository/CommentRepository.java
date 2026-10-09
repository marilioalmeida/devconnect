package com.devconnect.api.comment.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devconnect.api.comment.domain.Comment;
import com.devconnect.api.post.repository.PostCount;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("""
		select new com.devconnect.api.post.repository.PostCount(c.post.id, count(c.id))
		from Comment c
		where c.post.id in :postIds
		  and c.author.active = true
		group by c.post.id
		""")
	List<PostCount> countByPosts(@Param("postIds") Collection<Long> postIds);

	@Query(value = """
		select c
		from Comment c
		join fetch c.author
		where c.post.id = :postId
		  and c.author.active = true
		order by c.createdAt desc, c.id desc
		""",
		countQuery = """
		select count(c)
		from Comment c
		where c.post.id = :postId
		  and c.author.active = true
		""")
	Page<Comment> findByPost(@Param("postId") Long postId, Pageable pageable);

}
