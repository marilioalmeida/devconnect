package com.devconnect.api.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.devconnect.api.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByEmailAndActiveTrue(String email);

	boolean existsByEmail(String email);

	@Query(value = """
		select u
		from User u
		where u.id <> :userId
		  and u.active = true
		  and (lower(u.fullName) like lower(concat('%', :search, '%'))
		    or lower(u.email) like lower(concat('%', :search, '%')))
		order by u.fullName, u.id
		""",
		countQuery = """
		select count(u)
		from User u
		where u.id <> :userId
		  and u.active = true
		  and (lower(u.fullName) like lower(concat('%', :search, '%'))
		    or lower(u.email) like lower(concat('%', :search, '%')))
		""")
	Page<User> search(
		@Param("userId") Long userId,
		@Param("search") String search,
		Pageable pageable);

}
