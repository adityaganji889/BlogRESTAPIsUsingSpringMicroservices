package com.services.UserService.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="username",nullable=false,unique=true)
    private String username;
    
    @Column(name="password",nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role roles; // Use Role enum

    // Getters and Setters
}
