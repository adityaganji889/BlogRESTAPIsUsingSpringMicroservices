package com.services.UserService.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
@SecurityRequirement(name = "BearerAuth") // Should match the SecurityScheme name
@Tag(name = "Admin APIs")
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
	
	public DefaultResponse getResponseOfDeletedUserBlogs(HttpEntity<String> entity, Long id) {
		DefaultResponse defaultResponse = new DefaultResponse();
		try {
			defaultResponse = restTemplate.exchange("http://BLOG-SERVICE/api/blogs/deleteBlogsOfDeletedUser/" + id,
					HttpMethod.DELETE, entity, DefaultResponse.class).getBody();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultResponse;
	}
	
	@GetMapping("/getAllUsers")
	public ResponseEntity<ListOfUsersResponseDTO> getAllUsers(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader) {
		String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
		if (username.equals("Invalid Token")) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<User> user = userService.findByUsername(username);
		List<UserInfo> listOfUsers = new ArrayList<UserInfo>();
		ListOfUsersResponseDTO listOfUsersResponse = new ListOfUsersResponseDTO();
		if (user.isPresent() && user.get().getRoles().equals(Role.ADMIN)) {
			List<User> users = userService.getAllUsers();
			if(users!=null) {
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
			}
			else {
				listOfUsers = null;
				listOfUsersResponse.setSuccess(false);
				listOfUsersResponse.setMessage("No users to display.");
				listOfUsersResponse.setUsers(null);
				return new ResponseEntity<ListOfUsersResponseDTO>(listOfUsersResponse, HttpStatus.NOT_FOUND);
			}
		} else {
			listOfUsers = null;
			listOfUsersResponse.setSuccess(false);
			listOfUsersResponse.setMessage("You're not an Admin user, trying to access all users info.");
			listOfUsersResponse.setUsers(null);
			return new ResponseEntity<ListOfUsersResponseDTO>(listOfUsersResponse, HttpStatus.FORBIDDEN);
		}
	}
	
	@PutMapping("/updateUserRoleToAdmin/{id}")
	public ResponseEntity<UserInfoResponse> updateUserRoleToAdminById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		UserInfo userInfo = new UserInfo();
		String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
		if (username.equals("Invalid Token")) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		User user = userService.getUserInfoById(id);
		Role role = user.getRoles();
		User user1 = userService.getUserInfo();
		if (user != null) {
			User updatedUser = null;
			if(user1.getRoles().equals(Role.ADMIN)) {
				 updatedUser = userService.updateUserRoleToAdminById(id);
			}
			else {
				userInfo = null;
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage("You're not an Admin User, trying to update other user role to Admin.");
				userInfoResponse.setUserInfo(null);
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
			}
			else {
				userInfo.setId(user.getId());
				userInfo.setUsername(user.getUsername());
				userInfo.setRole(user.getRoles());
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage("User with id: "+ id + " is already admin.");
				userInfoResponse.setUserInfo(userInfo);
				return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
			}
		} else {
			userInfo = null;
			userInfoResponse.setSuccess(false);
			userInfoResponse.setMessage("User not found.");
			userInfoResponse.setUserInfo(null);
			return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
		}
	}
	
	@PutMapping("/updateUserRoleToUser/{id}")
	public ResponseEntity<UserInfoResponse> updateUserRoleToUserById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		UserInfo userInfo = new UserInfo();
		String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
		if (username.equals("Invalid Token")) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		User user = userService.getUserInfoById(id);
		Role role = user.getRoles();
		User user1 = userService.getUserInfo();
		if (user != null) {
			User updatedUser = null;
			if(user1.getRoles().equals(Role.ADMIN)) {
				 updatedUser = userService.updateUserRoleToUserById(id);
			}
			else {
				userInfo = null;
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage("You're not an Admin User, trying to update other user role to User.");
				userInfoResponse.setUserInfo(null);
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
			}
			else {
				userInfo.setId(user.getId());
				userInfo.setUsername(user.getUsername());
				userInfo.setRole(user.getRoles());
				userInfoResponse.setSuccess(false);
				userInfoResponse.setMessage("User with id: "+ id + " is already a normal user.");
				userInfoResponse.setUserInfo(userInfo);
				return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
			}
		} 
		else {
			userInfo = null;
			userInfoResponse.setSuccess(false);
			userInfoResponse.setMessage("User not found.");
			userInfoResponse.setUserInfo(null);
			return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.NOT_FOUND);
		}
	}
	
	@DeleteMapping("/deleteUserById/{id}")
	public ResponseEntity<UserInfoResponse> deleteUserById(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) @Parameter(hidden = true) String authorizationHeader,
			@PathVariable("id") Long id) {
		UserInfoResponse userInfoResponse = new UserInfoResponse();
		String username = loggedInUsername.getUsernameFromLoggedInToken(authorizationHeader);
		if (username.equals("Invalid Token")) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		HttpEntity<String> entity = authorizeHeaders(authorizationHeader);
		User user = userService.getUserInfoById(id);
		User user1 = userService.getUserInfo();
		if (user != null && user.getId() != user1.getId()) {
			if(user1.getRoles().equals(Role.ADMIN)) {
				getResponseOfDeletedUserBlogs(entity, id);
				userService.deleteUserById(id);
				userInfoResponse.setSuccess(true);
				userInfoResponse.setMessage("User with id:"+id+" is deleted successfully.");
				userInfoResponse.setUserInfo(null);
				return new ResponseEntity<UserInfoResponse>(userInfoResponse, HttpStatus.OK);
			}
			else {
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

}
