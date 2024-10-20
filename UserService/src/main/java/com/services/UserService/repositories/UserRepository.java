package com.services.UserService.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.services.UserService.entities.User;

public interface UserRepository extends JpaRepository<User,Long> {
	
	Optional<User> findByUsername(String username);

}
