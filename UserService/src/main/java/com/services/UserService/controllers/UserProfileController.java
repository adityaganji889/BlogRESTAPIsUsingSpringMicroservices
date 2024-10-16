package com.services.UserService.controllers;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.services.UserService.dtos.UserInfo;
import com.services.UserService.dtos.UserInfoResponse;
import com.services.UserService.entities.User;
import com.services.UserService.services.UserService;
import com.services.UserService.utils.JwtUtil;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/userProfile")
@CrossOrigin("*")
@SecurityRequirement(name="BearerAuth")//Should match the SecurityScheme name
@Tag(name="User APIs")
public class UserProfileController {

	@Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    
    @GetMapping("/protected-resource")
    public ResponseEntity<String> getProtectedResource(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                return ResponseEntity.ok("Protected Resource Accessed");
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
        
    @GetMapping("/userDetails")
    public ResponseEntity<UserInfoResponse> getUserDetails(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader) {
    	if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            UserInfoResponse userInfoResponse = new UserInfoResponse();
        	UserInfo userInfo = new UserInfo();
            if (jwtUtil.validateToken(token)) {
            	String username = jwtUtil.extractUsername(token);
            	Optional<User> user = userService.findByUsername(username);
            	if(user.isPresent()) {
            		userInfo.setId(user.get().getId());
            		userInfo.setUsername(user.get().getUsername());
            		userInfo.setRole(user.get().getRoles());
            		userInfoResponse.setSuccess(true);
            		userInfoResponse.setMessage("Logged in user info fetched successfully.");
            	    userInfoResponse.setUserInfo(userInfo);
            	    return new ResponseEntity<UserInfoResponse>(userInfoResponse,HttpStatus.OK);
            	}
            	else {
            		userInfo = null;
            		userInfoResponse.setSuccess(false);
            		userInfoResponse.setMessage("User not found.");
            	    userInfoResponse.setUserInfo(null);
            		return new ResponseEntity<UserInfoResponse>(userInfoResponse,HttpStatus.NOT_FOUND);
            	}
            }
        }
    		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
   
    
    	
    }
    
    @GetMapping("/userDetails/{id}")
    public ResponseEntity<UserInfoResponse> getUserDetailsById(@RequestHeader(value=HttpHeaders.AUTHORIZATION, required=false) String authorizationHeader,@PathVariable("id") Long id) {
    	if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            UserInfoResponse userInfoResponse = new UserInfoResponse();
        	UserInfo userInfo = new UserInfo();
            if (jwtUtil.validateToken(token)) {
            	String username = jwtUtil.extractUsername(token);
//            	Optional<User> user = userService.findByUsername(username);
            	User user = userService.getUserInfoById(id);
            	if(user!=null) {
            		userInfo.setId(user.getId());
            		userInfo.setUsername(user.getUsername());
            		userInfo.setRole(user.getRoles());
            		userInfoResponse.setSuccess(true);
            		userInfoResponse.setMessage("User info with id: "+id+" fetched successfully.");
            	    userInfoResponse.setUserInfo(userInfo);
            	    return new ResponseEntity<UserInfoResponse>(userInfoResponse,HttpStatus.OK);
            	}
            	else {
            		userInfo = null;
            		userInfoResponse.setSuccess(false);
            		userInfoResponse.setMessage("User not found.");
            	    userInfoResponse.setUserInfo(null);
            		return new ResponseEntity<UserInfoResponse>(userInfoResponse,HttpStatus.NOT_FOUND);
            	}
            }
        }
    		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
   
    
    	
    }
	
}
