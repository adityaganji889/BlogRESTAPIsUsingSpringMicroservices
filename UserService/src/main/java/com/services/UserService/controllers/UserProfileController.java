package com.services.UserService.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.services.UserService.utils.LoggedInUsernameExtractionUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/userProfile")
@CrossOrigin("*")
@SecurityRequirement(name = "BearerAuth") // Should match the SecurityScheme name
@Tag(name = "User APIs", description = "APIs for user profile management, including fetching and updating user details.")
public class UserProfileController {

	@Autowired
	private UserService userService;

	@Autowired
	private LoggedInUsernameExtractionUtil loggedInUsername;

	@Operation(
			summary = "Get logged-in user details",
			description = "Fetches details of the currently logged-in user.",
			responses = {
				@ApiResponse(responseCode = "200", description = "Logged-in user info fetched successfully",
					content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
				@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
				@ApiResponse(responseCode = "404", description = "User not found", content = @Content)
			}
		)
	@GetMapping("/userDetails")
	public ResponseEntity<UserInfoResponse> getUserDetails(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		UserInfo userInfo = new UserInfo();
		String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
		if (username.equals("Invalid Token")) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<User> user = userService.findByUsername(username);
		if (user.isPresent()) {
			userInfo.setId(user.get().getId());
			userInfo.setUsername(user.get().getUsername());
			userInfo.setRole(user.get().getRoles());
			userInfoResponse.setSuccess(true);
			userInfoResponse.setMessage("Logged in user info fetched successfully.");
			userInfoResponse.setUserInfo(userInfo);
			return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.OK);
		} else {
			userInfo = null;
			userInfoResponse.setSuccess(false);
			userInfoResponse.setMessage("User not found.");
			userInfoResponse.setUserInfo(null);
			return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
		}
	}

	@Operation(
			summary = "Get user details by ID",
			description = "Fetches details of a user by their ID.",
			parameters = {
				@Parameter(name = "id", description = "ID of the user to fetch", required = true)
			},
			responses = {
				@ApiResponse(responseCode = "200", description = "User info with specified ID fetched successfully",
					content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
				@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
				@ApiResponse(responseCode = "404", description = "User not found", content = @Content)
			}
		)
	@GetMapping("/userDetails/{id}")
	public ResponseEntity<UserInfoResponse> getUserDetailsById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		UserInfo userInfo = new UserInfo();
		String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
		if (username.equals("Invalid Token")) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		User user = userService.getUserInfoById(id);
		if (user != null) {
			userInfo.setId(user.getId());
			userInfo.setUsername(user.getUsername());
			userInfo.setRole(user.getRoles());
			userInfoResponse.setSuccess(true);
			userInfoResponse.setMessage("User info with id: " + id + " fetched successfully.");
			userInfoResponse.setUserInfo(userInfo);
			return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.OK);
		} else {
			userInfo = null;
			userInfoResponse.setSuccess(false);
			userInfoResponse.setMessage("User not found.");
			userInfoResponse.setUserInfo(null);
			return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
		}

	}

}
