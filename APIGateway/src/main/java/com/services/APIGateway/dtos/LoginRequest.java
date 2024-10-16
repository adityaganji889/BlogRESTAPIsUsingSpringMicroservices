package com.services.APIGateway.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    
	private String username;
    private String password;

}
