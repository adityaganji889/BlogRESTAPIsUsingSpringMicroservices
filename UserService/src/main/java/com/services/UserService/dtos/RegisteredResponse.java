package com.services.UserService.dtos;

import com.services.UserService.entities.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredResponse {
    
	private Boolean success;
    private String message;
    private String username;
    private Role role;
    
}