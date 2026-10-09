package com.devconnect.api.friendship.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devconnect.api.friendship.domain.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

	@Query("""
		select f
		from Friendship f
		where (f.requester.id = :userId and f.recipient.id = :otherUserId)
		   or (f.requester.id = :otherUserId and f.recipient.id = :userId)
		""")
	Optional<Friendship> findBetween(
		@Param("userId") Long userId,
		@Param("otherUserId") Long otherUserId);

	@Query("""
		select f
		from Friendship f
		join fetch f.requester
		where f.recipient.id = :userId
		  and f.requester.active = true
		  and f.status = com.devconnect.api.friendship.domain.FriendshipStatus.PENDING
		order by f.requestedAt desc, f.id desc
		""")
	List<Friendship> findReceivedRequests(@Param("userId") Long userId);

	@Query(value = """
		select f
		from Friendship f
		join fetch f.requester rq
		join fetch f.recipient rc
		where f.status = com.devconnect.api.friendship.domain.FriendshipStatus.ACCEPTED
		  and rq.active = true
		  and rc.active = true
		  and (rq.id = :userId or rc.id = :userId)
		  and (
		       (rq.id <> :userId
		        and (lower(rq.fullName) like lower(concat('%', :search, '%'))
		          or lower(rq.email) like lower(concat('%', :search, '%'))))
		    or (rc.id <> :userId
		        and (lower(rc.fullName) like lower(concat('%', :search, '%'))
		          or lower(rc.email) like lower(concat('%', :search, '%'))))
		  )
		order by case when rq.id = :userId then rc.fullName else rq.fullName end, f.id
		""",
		countQuery = """
		select count(f)
		from Friendship f
		join f.requester rq
		join f.recipient rc
		where f.status = com.devconnect.api.friendship.domain.FriendshipStatus.ACCEPTED
		  and rq.active = true
		  and rc.active = true
		  and (rq.id = :userId or rc.id = :userId)
		  and (
		       (rq.id <> :userId
		        and (lower(rq.fullName) like lower(concat('%', :search, '%'))
		          or lower(rq.email) like lower(concat('%', :search, '%'))))
		    or (rc.id <> :userId
		        and (lower(rc.fullName) like lower(concat('%', :search, '%'))
		          or lower(rc.email) like lower(concat('%', :search, '%'))))
		  )
		""")
	Page<Friendship> findFriends(
		@Param("userId") Long userId,
		@Param("search") String search,
		Pageable pageable);

}
