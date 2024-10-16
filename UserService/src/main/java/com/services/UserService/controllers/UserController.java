package com.services.UserService.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.services.UserService.dtos.LoggedInResponse;
import com.services.UserService.dtos.LoginRequest;
import com.services.UserService.dtos.RegisteredResponse;
import com.services.UserService.dtos.UserRegistrationRequest;
import com.services.UserService.entities.User;
import com.services.UserService.services.UserService;
import com.services.UserService.utils.JwtUtil;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@Tag(name="Auth APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<RegisteredResponse> register(@RequestBody UserRegistrationRequest request) {
        userService.registerNewUser(request);
        RegisteredResponse registeredResponse = new RegisteredResponse();
        registeredResponse.setSuccess(true);
        registeredResponse.setMessage("User registered successfully");
        registeredResponse.setUsername(request.getUsername());
        registeredResponse.setRole(request.getRole());
        return ResponseEntity.ok(registeredResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<LoggedInResponse> authenticate(@RequestBody LoginRequest request) {
        Optional<User> userp = userService.findByUsername(request.getUsername());
        LoggedInResponse loggedInResponse = new LoggedInResponse();
        if(userp.isPresent()) {
        	User user = userp.get();
        	if (user != null && userService.getPasswordEncoder().matches(request.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user);
                loggedInResponse.setToken(token);
                loggedInResponse.setMessage("Your logged in successfully.");
                loggedInResponse.setSuccess(true);
                loggedInResponse.setUsername(request.getUsername());
                return ResponseEntity.ok(loggedInResponse);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
