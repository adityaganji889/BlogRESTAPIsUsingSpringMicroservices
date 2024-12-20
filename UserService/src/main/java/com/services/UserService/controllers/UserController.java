package com.services.UserService.controllers;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.services.UserService.dtos.ChangePassword;
import com.services.UserService.dtos.DefaultResponse;
import com.services.UserService.dtos.LoggedInResponse;
import com.services.UserService.dtos.LoginRequest;
import com.services.UserService.dtos.RegisteredResponse;
import com.services.UserService.dtos.UserRegistrationRequest;
import com.services.UserService.entities.Otp;
import com.services.UserService.entities.User;
import com.services.UserService.services.UserService;
import com.services.UserService.utils.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
                             content = @Content),
                @ApiResponse(responseCode = "500", description = "Internal Server Error - Error occurred during register", 
                content = @Content(schema = @Schema(implementation = RegisteredResponse.class))),
            },
            security = {}
        )
    @PostMapping("/register")
    public ResponseEntity<RegisteredResponse> register(@RequestBody UserRegistrationRequest request) {
        // To make it async annotated controller with @EnableAsync, further added wrapped return type of this method in CompletableFuture<> and moved the synchronous code block to asynchronous code block wrapping it with return of CompletableFuture() object used thenApply, exceptionally methods in it.
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
                             content = @Content),
                @ApiResponse(responseCode = "500", description = "Internal Server Error - Error occurred during login", 
                content = @Content(schema = @Schema(implementation = LoggedInResponse.class))),
            },
            security = {}
        )
    @PostMapping("/authenticate")
    public ResponseEntity<LoggedInResponse> authenticate(@RequestBody LoginRequest request) {
        	LoggedInResponse loggedInResponse = new LoggedInResponse();
        	try {
            	CompletableFuture<Optional<User>> cfu = userService.findByUsername(request.getUsername());
            	Optional<User> userp = cfu.get();
                if (userp.isPresent()) {
                    User user = userp.get();
                    if (user != null && userService.getPasswordEncoder().matches(request.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user);
                        loggedInResponse.setToken(token);
                        loggedInResponse.setMessage("You logged in successfully.");
                        loggedInResponse.setSuccess(true);
                        loggedInResponse.setUsername(request.getUsername());
                        return ResponseEntity.ok(loggedInResponse);
                    }
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            catch(Exception e) {
            	e.printStackTrace();
            	loggedInResponse.setToken(null);
                loggedInResponse.setMessage(e.getMessage());
                loggedInResponse.setSuccess(false);
                loggedInResponse.setUsername(null);
                return new ResponseEntity<LoggedInResponse>(loggedInResponse,HttpStatus.INTERNAL_SERVER_ERROR);
            } 
    }
    
 // send mail for email verification
    @Operation(
            summary = "Verify Email",
            description = "Sends an OTP to the provided email for verification.",
            parameters = @Parameter(name = "email", description = "User email for verification", required = true),
            responses = {
                @ApiResponse(responseCode = "200", description = "Email containing OTP sent successfully", 
                             content = @Content(schema = @Schema(implementation = DefaultResponse.class))),
                @ApiResponse(responseCode = "404", description = "User not found with the provided email", 
                             content = @Content),
                @ApiResponse(responseCode = "500", description = "Internal Server Error", 
                             content = @Content)
            },
            security = {}
        )
    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<DefaultResponse> verifyEmail(@PathVariable String email) {
        CompletableFuture<Optional<User>> cfu = userService.findByUsername(email);
        Optional<User> user = null;
        try {
        	user = cfu.get();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        if(user.isPresent()) {
        	
        	CompletableFuture<Otp> otpR = userService.verifyEmailSend(email, user.get());
            Otp otp = null;
            try {
            	otp = otpR.get();
            	return ResponseEntity.ok(new DefaultResponse(true,"Email containing OTP sent successfully to:"+ email +"for verification"));	
            }
            catch(Exception e) {
            	e.printStackTrace();
            	new ResponseEntity<>(new DefaultResponse(false, e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(new DefaultResponse(false, "User with the email id: "+email+" is not found"),HttpStatus.NOT_FOUND);
    }

    @Operation(
            summary = "Verify OTP",
            description = "Verifies the provided OTP against the user's email.",
            parameters = {
                @Parameter(name = "otp", description = "The OTP to verify", required = true),
                @Parameter(name = "email", description = "User email associated with the OTP", required = true)
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "OTP verified successfully", 
                             content = @Content(schema = @Schema(implementation = DefaultResponse.class))),
                @ApiResponse(responseCode = "404", description = "OTP not found", 
                             content = @Content),
                @ApiResponse(responseCode = "417", description = "OTP has expired", 
                             content = @Content),
                @ApiResponse(responseCode = "500", description = "Internal Server Error", 
                             content = @Content)
            },
            security = {}
        )
    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<DefaultResponse> verifyOtp(@PathVariable("otp") Integer otpValue, @PathVariable String email) {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Please provide an valid email!"));

    	CompletableFuture<Optional<User>> cfu = userService.findByUsername(email);
        Optional<User> user = null;
        try {
        	user = cfu.get();
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        if(user.isPresent()) {
        	CompletableFuture<Optional<Otp>> otpR = userService.findByOtpAndUser(otpValue, user.get());
            Optional<Otp> otp = null;
            try {
            	otp = otpR.get();
            }
            catch(Exception e) {
            	e.printStackTrace();
            	new ResponseEntity<>(new DefaultResponse(false, e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            if (otp.get().getExpirationTime().before(Date.from(Instant.now()))) {
                userService.deleteOtpById(otp.get().getFpid());
                return new ResponseEntity<DefaultResponse>(new DefaultResponse(false, "OTP Has Expired"),HttpStatus.EXPECTATION_FAILED);
            }
            
            userService.deleteOtpById(otp.get().getFpid());
            return ResponseEntity.ok(new DefaultResponse(true,"OTP verified for email: "+ user.get().getUsername()+" successfully."));	
        }

        return new ResponseEntity<DefaultResponse>(new DefaultResponse(false, "OTP Not Found"),HttpStatus.NOT_FOUND);
    }


    @Operation(
            summary = "Change Password",
            description = "Changes the password for the user associated with the provided email.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "New password details",
                content = @Content(schema = @Schema(implementation = ChangePassword.class))
            ),
            parameters = @Parameter(name = "email", description = "User email to change the password", required = true),
            responses = {
                @ApiResponse(responseCode = "200", description = "Password changed successfully", 
                             content = @Content(schema = @Schema(implementation = DefaultResponse.class))),
                @ApiResponse(responseCode = "417", description = "Passwords do not match", 
                             content = @Content),
                @ApiResponse(responseCode = "500", description = "Internal Server Error", 
                             content = @Content)
            }
        )
    @PostMapping("/changePassword/{email}")
    public ResponseEntity<DefaultResponse> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email) {
        
        CompletableFuture<Boolean> cfu = userService.changePasswordHandle(email, changePassword.password(), changePassword.repeatPassword());
        Boolean flag = false;
        try {
        	flag = cfu.get();	
        }
        catch(Exception e) {
        	e.printStackTrace();
        	new ResponseEntity<>(new DefaultResponse(false, e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(flag) {
        	return ResponseEntity.ok(new DefaultResponse(true,"Password is resetted for email: "+email+" successfully."));
        }
        else {
        	return new ResponseEntity<DefaultResponse>(new DefaultResponse(false,"Please enter the password again!"), HttpStatus.EXPECTATION_FAILED);
        }
    }

}
