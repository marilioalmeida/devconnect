package com.devconnect.api.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devconnect.api.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

}
