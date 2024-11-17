package com.services.UserService.repositories;

import java.util.Optional;
//import java.util.concurrent.CompletableFuture;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.services.UserService.entities.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User,Long> {
	
//	@Async
	Optional<User> findByUsername(String username);
	
//	Optional<User> findByEmail(String email);
	
	// Create a new method for saving users asynchronously
//    @Async
//    default CompletableFuture<Void> saveUser(User user) {
//        return CompletableFuture.runAsync(() -> save(user));
//    }

	@Transactional
    @Modifying
    @Query("update User u set u.password = ?2 where u.username = ?1")
    void updatePassword(String email, String password);

}
