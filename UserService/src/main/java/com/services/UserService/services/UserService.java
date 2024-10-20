package com.services.UserService.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.services.UserService.dtos.UserRegistrationRequest;
import com.services.UserService.entities.Role;
import com.services.UserService.entities.User;
import com.services.UserService.repositories.UserRepository;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    

    public void registerNewUser(UserRegistrationRequest request) {
        
    	Optional<User> userExists = userRepository.findByUsername(request.getUsername());
    	if(!userExists.isPresent()) {
    		User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
//            user.setRoles(Collections.singleton(request.getRole())); // Set role using enum
            if(request.getRole()==Role.USER) {
              user.setRoles(Role.USER);	
            }
            else {
              user.setRoles(Role.ADMIN);
            }
            userRepository.save(user);	
    	}
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserInfo() {
    	User user = new User();
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		var userInfo = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));;
 		if(userInfo!=null) {
 			user.setId(userInfo.getId());
 			user.setUsername(userInfo.getUsername());
 			user.setPassword(userInfo.getPassword());
 			user.setRoles(userInfo.getRoles());
 			return user;
 		}
		return null;
    }
    
    public User getUserInfoById(Long id) {
    	Optional<User> user = userRepository.findById(id);
    	if(user.isPresent()) {
    		return user.get();
    	}
		return null;
    }
    
    public void deleteUserById(Long id) {
    	Optional<User> user = userRepository.findById(id);
    	if(user.isPresent()) {
    		userRepository.deleteById(user.get().getId());
    	}
    }
    
    public User updateUserRoleToAdminById(Long id) {
    	Optional<User> user = userRepository.findById(id);
    	User user1 = getUserInfo();
    	if(user.isPresent()) {
    		User user2 = user.get();
    		if(user2.getId() != user1.getId() && user2.getRoles().equals(Role.USER)) {
    			user2.setRoles(Role.ADMIN);
        		User updatedUser = userRepository.save(user2);
        		return updatedUser;    			
    		}
    		else {
    			return user2;
    		}
    	}
		return null;
    }
    
    public User updateUserRoleToUserById(Long id) {
    	Optional<User> user = userRepository.findById(id);
    	User user1 = getUserInfo();
    	if(user.isPresent()) {
    		User user2 = user.get();
    		if(user2.getId() != user1.getId() && user2.getRoles().equals(Role.ADMIN)) {
    			user2.setRoles(Role.USER);
        		User updatedUser = userRepository.save(user2);
        		return updatedUser;    			
    		}
    		else {
    			return user2;
    		}
    	}
		return null;
    }
    
    public List<User> getAllUsers () {
    	List<User> users = userRepository.findAll();
    	if(users.isEmpty()) {
    		return null;
    	}
        return users;
    }
    
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
    
}