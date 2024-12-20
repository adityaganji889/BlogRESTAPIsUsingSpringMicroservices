package com.services.UserService.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.services.UserService.dtos.DefaultResponse;
import com.services.UserService.dtos.ListOfUsersResponseDTO;
import com.services.UserService.dtos.UserInfo;
import com.services.UserService.dtos.UserInfoResponse;
import com.services.UserService.entities.Role;
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
@RequestMapping("/api/admin")
@CrossOrigin("*")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Admin APIs", description = "APIs for admin operations, including managing users and their permissions.")
public class AdminController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private LoggedInUsernameExtractionUtil loggedInUsername;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public HttpEntity<String> authorizeHeaders(String authorizedHeader) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authorizedHeader);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		return entity;
	}
	
	public CompletableFuture<DefaultResponse> getResponseOfDeletedUserBlogs(HttpEntity<String> entity, Long id) {
		return CompletableFuture.supplyAsync(() -> {
			DefaultResponse defaultResponse = new DefaultResponse();
			try {
				defaultResponse = restTemplate.exchange("http://BLOG-SERVICE/api/blogs/deleteBlogsOfDeletedUser/" + id,
						HttpMethod.DELETE, entity, DefaultResponse.class).getBody();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return defaultResponse;
		});
	}
	
	@Operation(
			summary = "Get all users",
			description = "Fetches a list of all users in the system. Accessible only by admin.",
			responses = {
				@ApiResponse(responseCode = "200", description = "All users fetched successfully",
					content = @Content(schema = @Schema(implementation = ListOfUsersResponseDTO.class))),
				@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
				@ApiResponse(responseCode = "403", description = "Forbidden - Not an admin user", content = @Content),
				@ApiResponse(responseCode = "404", description = "No users found", content = @Content)
			}
		)
	@GetMapping("/getAllUsers")
	public ResponseEntity<ListOfUsersResponseDTO> getAllUsers(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader) {
			ListOfUsersResponseDTO listOfUsersResponse = new ListOfUsersResponseDTO();
			try {
				String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
				if (username.equals("Invalid Token")) {
					return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				}
				CompletableFuture<Optional<User>> cfu = userService.findByUsername(username);
				Optional<User> user = cfu.get();
				List<UserInfo> listOfUsers = new ArrayList<UserInfo>();
				if (user.isPresent() && user.get().getRoles().equals(Role.ADMIN)) {
					CompletableFuture<List<User>> cfus = userService.getAllUsers(); 
					List<User> users = cfus.get();
					if(users != null) {
						for(User u: users) {
							UserInfo userInfo = new UserInfo();
							userInfo.setId(u.getId());
							userInfo.setUsername(u.getUsername());
							userInfo.setRole(u.getRoles());
							listOfUsers.add(userInfo);
						}
						listOfUsersResponse.setSuccess(true);
						listOfUsersResponse.setMessage("All users info fetched successfully.");
						listOfUsersResponse.setUsers(listOfUsers);
						return new ResponseEntity<ListOfUsersResponseDTO>(listOfUsersResponse, HttpStatus.OK);
					} else {
						listOfUsersResponse.setSuccess(false);
						listOfUsersResponse.setMessage("No users to display.");
						listOfUsersResponse.setUsers(null);
						return new ResponseEntity<ListOfUsersResponseDTO>(listOfUsersResponse, HttpStatus.NOT_FOUND);
					}
				} else {
					listOfUsersResponse.setSuccess(false);
					listOfUsersResponse.setMessage("You're not an Admin user, trying to access all users info.");
					listOfUsersResponse.setUsers(null);
					return new ResponseEntity<ListOfUsersResponseDTO>(listOfUsersResponse, HttpStatus.FORBIDDEN);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
				listOfUsersResponse.setSuccess(false);
				listOfUsersResponse.setMessage(e.getMessage());
				listOfUsersResponse.setUsers(null);
				return new ResponseEntity<ListOfUsersResponseDTO>(listOfUsersResponse,HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	
	@SuppressWarnings("unused")
	@Operation(
			summary = "Update user role to Admin",
			description = "Updates the role of a user to Admin. Accessible only by admin.",
			parameters = {
				@Parameter(name = "id", description = "ID of the user to update", required = true)
			},
			responses = {
				@ApiResponse(responseCode = "200", description = "User role updated to admin successfully",
					content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
				@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
				@ApiResponse(responseCode = "403", description = "Forbidden - Not an admin user", content = @Content),
				@ApiResponse(responseCode = "404", description = "User not found or already an admin", content = @Content)
			}
		)
	@PutMapping("/updateUserRoleToAdmin/{id}")
	public ResponseEntity<UserInfoResponse> updateUserRoleToAdminById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
			UserInfoResponse userInfoResponse = new UserInfoResponse();
			try {
				UserInfo userInfo = new UserInfo();
				String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
				if (username.equals("Invalid Token")) {
					return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				}
				CompletableFuture<User> cfu = userService.getUserInfoById(id);
				User user = cfu.get();
				Role role = user.getRoles();
				CompletableFuture<User> cfu1 = userService.getUserInfo();
				User user1 = cfu1.get();
				if (user != null) {
					User updatedUser = null;
					if(user1.getRoles().equals(Role.ADMIN)) {
						 CompletableFuture<User> cfu2 = userService.updateUserRoleToAdminById(id);
						 updatedUser = cfu2.get();
					} else {
						userInfoResponse.setSuccess(false);
						userInfoResponse.setMessage("You're not an Admin User, trying to update other user role to Admin.");
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.FORBIDDEN);
					}
					if(updatedUser != null && role.equals(Role.USER)) {
						userInfo.setId(updatedUser.getId());
						userInfo.setUsername(updatedUser.getUsername());
						userInfo.setRole(updatedUser.getRoles());
						userInfoResponse.setSuccess(true);
						userInfoResponse.setMessage("Role of User with id: " + id + " is updated successfully.");
						userInfoResponse.setUserInfo(userInfo);
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.OK);
					} else {
						userInfo.setId(user.getId());
						userInfo.setUsername(user.getUsername());
						userInfo.setRole(user.getRoles());
						userInfoResponse.setSuccess(false);
						userInfoResponse.setMessage("User with id: "+ id + " is already admin.");
						userInfoResponse.setUserInfo(userInfo);
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
					}
				} else {
					userInfoResponse.setSuccess(false);
					userInfoResponse.setMessage("User not found.");
					userInfoResponse.setUserInfo(null);
					return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage(e.getMessage());
				userInfoResponse.setUserInfo(null);
				return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	
	@SuppressWarnings("unused")
	@Operation(
			summary = "Update user role to User",
			description = "Updates the role of a user to normal user. Accessible only by admin.",
			parameters = {
				@Parameter(name = "id", description = "ID of the user to update", required = true)
			},
			responses = {
				@ApiResponse(responseCode = "200", description = "User role updated to user successfully",
					content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
				@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
				@ApiResponse(responseCode = "403", description = "Forbidden - Not an admin user", content = @Content),
				@ApiResponse(responseCode = "404", description = "User not found or already a normal user", content = @Content)
			}
		)
	@PutMapping("/updateUserRoleToUser/{id}")
	public ResponseEntity<UserInfoResponse> updateUserRoleToUserById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
			UserInfoResponse userInfoResponse = new UserInfoResponse();
			try {
				UserInfo userInfo = new UserInfo();
				String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
				if (username.equals("Invalid Token")) {
					return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				}
				CompletableFuture<User> cfu = userService.getUserInfoById(id);
				User user = cfu.get();
				Role role = user.getRoles();
				CompletableFuture<User> cfu1 = userService.getUserInfo();
				User user1 = cfu1.get();
				if (user != null) {
					User updatedUser = null;
					if(user1.getRoles().equals(Role.ADMIN)) {
						 CompletableFuture<User> cfu2 = userService.updateUserRoleToUserById(id);
						 updatedUser = cfu2.get();
					} else {
						userInfoResponse.setSuccess(false);
						userInfoResponse.setMessage("You're not an Admin User, trying to update other user role to User.");
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.FORBIDDEN);
					}
					if(updatedUser != null && role.equals(Role.ADMIN)) {
						userInfo.setId(updatedUser.getId());
						userInfo.setUsername(updatedUser.getUsername());
						userInfo.setRole(updatedUser.getRoles());
						userInfoResponse.setSuccess(true);
						userInfoResponse.setMessage("Role of User with id: " + id + " is updated successfully.");
						userInfoResponse.setUserInfo(userInfo);
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.OK);
					} else {
						userInfo.setId(user.getId());
						userInfo.setUsername(user.getUsername());
						userInfo.setRole(user.getRoles());
						userInfoResponse.setSuccess(false);
						userInfoResponse.setMessage("User with id: "+ id + " is already a normal user.");
						userInfoResponse.setUserInfo(userInfo);
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
					}
				} else {
					userInfoResponse.setSuccess(false);
					userInfoResponse.setMessage("User not found.");
					userInfoResponse.setUserInfo(null);
					return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
				}
			}
			catch(Exception e) {
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage(e.getMessage());
				userInfoResponse.setUserInfo(null);
				return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
	
	@Operation(
			summary = "Delete user by ID",
			description = "Deletes a user from the system by ID. Accessible only by admin.",
			parameters = {
				@Parameter(name = "id", description = "ID of the user to delete", required = true)
			},
			responses = {
				@ApiResponse(responseCode = "200", description = "User deleted successfully",
					content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
				@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid token", content = @Content),
				@ApiResponse(responseCode = "403", description = "Forbidden - Not an admin user", content = @Content),
				@ApiResponse(responseCode = "404", description = "User not found or trying to delete yourself", content = @Content)
			}
		)
	@DeleteMapping("/deleteUserById/{id}")
	public ResponseEntity<UserInfoResponse> deleteUserById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
			UserInfoResponse userInfoResponse = new UserInfoResponse();
			try {
				String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
				if (username.equals("Invalid Token")) {
					return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				}
				HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
				CompletableFuture<User> cfu = userService.getUserInfoById(id);
				User user = cfu.get();
				CompletableFuture<User> cfu1 = userService.getUserInfo();
				User user1 = cfu1.get();
				if (user != null && user.getId() != user1.getId()) {
					if(user1.getRoles().equals(Role.ADMIN)) {
						getResponseOfDeletedUserBlogs(entity, id);
						userService.deleteUserById(id);
						userInfoResponse.setSuccess(true);
						userInfoResponse.setMessage("User with id:"+id+" is deleted successfully.");
						userInfoResponse.setUserInfo(null);
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.OK);
					} else {
						userInfoResponse.setSuccess(false);
						userInfoResponse.setMessage("You're not an Admin User, trying to update other user role to User.");
						userInfoResponse.setUserInfo(null);
						return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.FORBIDDEN);
					}
				} else {
					userInfoResponse.setSuccess(false);
					userInfoResponse.setMessage("User not found. Or you're trying to delete yourself.");
					userInfoResponse.setUserInfo(null);
					return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
				}
			}
			catch(Exception e) {
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage(e.getMessage());
				userInfoResponse.setUserInfo(null);
				return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.INTERNAL_SERVER_ERROR);
			}
	}
}