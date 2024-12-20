package com.services.APIGateway.controllers;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.services.APIGateway.dtos.LoggedInResponse;
import com.services.APIGateway.dtos.LoginRequest;
import com.services.APIGateway.dtos.LogoutRequest;
import com.services.APIGateway.dtos.LogoutResponse;
import com.services.APIGateway.dtos.RegisteredResponse;
import com.services.APIGateway.dtos.RegistrationRequestDTO;
import com.services.APIGateway.dtos.Role;
import com.services.APIGateway.dtos.UserRegistrationRequest;
import com.services.APIGateway.services.TokenStore;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private TokenStore tokenStore;

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<LoggedInResponse>> login(@RequestBody LoginRequest request) {
    	
    	return CompletableFuture.supplyAsync(() -> {
    		String url = "http://USER-SERVICE/api/users/authenticate";
        	
        	LoggedInResponse loggedInResponse = restTemplate.postForObject(url, request, LoggedInResponse.class);
            
        	
        	tokenStore.storeToken(loggedInResponse.getUsername(),loggedInResponse.getToken());
        	
        	return new ResponseEntity<LoggedInResponse>(loggedInResponse,HttpStatus.OK);
    	});
    }

    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<RegisteredResponse>> register(@RequestBody UserRegistrationRequest request) {
        
    	return CompletableFuture.supplyAsync(() -> {
    		System.out.println("request:"+ request);
        	
        	RegistrationRequestDTO newRequest = new RegistrationRequestDTO();
        	
        	newRequest.setUsername(request.getUsername());
        	newRequest.setPassword(request.getPassword());
        	if ("USER".equals(request.getRole())) {
        	    newRequest.setRole(Role.USER);
        	} else {
        	    newRequest.setRole(Role.ADMIN);
        	}
        	
        	 String url = "http://USER-SERVICE/api/users/register";
        	 RegisteredResponse registeredResponse = restTemplate.postForObject(url, newRequest, RegisteredResponse.class);

        	 return new ResponseEntity<RegisteredResponse>(registeredResponse,HttpStatus.OK);
    	});
    }
    
    
    @PostMapping("/logout")
    public CompletableFuture<ResponseEntity<LogoutResponse>> logout(@RequestBody LogoutRequest request){
		
    	return CompletableFuture.supplyAsync(() -> {
    		String username = request.getUsername();
        	LogoutResponse logoutResponse = new LogoutResponse();
        	if(tokenStore.getToken(username)!=null) {
        		tokenStore.clearToken(username);
            	logoutResponse.setSuccess(true);
            	logoutResponse.setMessage("Your are logged out successfully.");
            	logoutResponse.setUsername(username);
        	}
        	else {
            	logoutResponse.setSuccess(false);
            	logoutResponse.setMessage("Your are already logged out.");
            	logoutResponse.setUsername(username);
        	}
        	return ResponseEntity.ok(logoutResponse);
    	});
    	
    }
    
}