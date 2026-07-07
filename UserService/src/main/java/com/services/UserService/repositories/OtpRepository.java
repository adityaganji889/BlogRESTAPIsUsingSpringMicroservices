package com.services.UserService.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.services.UserService.entities.Otp;
import com.services.UserService.entities.User;

import jakarta.transaction.Transactional;

public interface OtpRepository extends JpaRepository<Otp, Integer> {

    @Query("select fp from Otp fp where fp.otpValue = ?1 and fp.user = ?2")
    Optional<Otp> findByOtpAndUser(Integer otpValue, User user);

    @Transactional
    @Modifying
    @Query("delete from Otp o where o.user.id = ?1")
	public void deleteOtpByUser(Long userId);
    
}