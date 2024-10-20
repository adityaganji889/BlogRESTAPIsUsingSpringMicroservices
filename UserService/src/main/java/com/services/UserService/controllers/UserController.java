package com.services.UserService.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.services.UserService.dtos.LoggedInResponse;
import com.services.UserService.dtos.LoginRequest;
import com.services.UserService.dtos.RegisteredResponse;
import com.services.UserService.dtos.UserRegistrationRequest;
import com.services.UserService.entities.User;
import com.services.UserService.services.UserService;
import com.services.UserService.utils.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@Tag(name = "Auth APIs", description = "APIs for user authentication, including login and registration.")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(
            summary = "Register a new user",
            description = "This endpoint allows a new user to register in the system.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details",
                content = @Content(schema = @Schema(implementation = UserRegistrationRequest.class))
            ),
            responses = {
                @ApiResponse(responseCode = "200", description = "User registered successfully", 
                             content = @Content(schema = @Schema(implementation = RegisteredResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data", 
                             content = @Content)
            },
            security = {}
        )
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

    @Operation(
            summary = "Authenticate a user",
            description = "This endpoint allows an existing user to log in to the system.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User login credentials",
                content = @Content(schema = @Schema(implementation = LoginRequest.class))
            ),
            responses = {
                @ApiResponse(responseCode = "200", description = "User logged in successfully", 
                             content = @Content(schema = @Schema(implementation = LoggedInResponse.class))),
                @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", 
                             content = @Content)
            },
            security = {}
        )
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
