package com.services.UserService.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggedInUsernameExtractionUtil {
	
	@Autowired
	private JwtUtil jwtUtil;

	public String getUsernameFromLoggedInToken(String authorizationHeader) {
		String username = null;
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String token = authorizationHeader.substring(7);
			if (jwtUtil.validateToken(token)) {
				username = jwtUtil.extractUsername(token);
			} else {
				username = "Invalid Token";
			}
		}
		return username;
	}
}
