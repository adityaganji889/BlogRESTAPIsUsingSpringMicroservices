package com.services.UserService.services;

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
import com.services.UserService.utils.JwtUtil;


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
    
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
    
}