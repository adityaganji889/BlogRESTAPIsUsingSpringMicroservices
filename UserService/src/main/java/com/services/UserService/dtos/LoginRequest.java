package com.services.UserService.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;

    // Getters and Setters
}