package com.services.UserService.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

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
