package com.services.UserService.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.services.UserService.services.CustomUserDetailsService;
import com.services.UserService.utils.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil; // JWT utility for token operations

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
    	String authHeader = request.getHeader("Authorization");
        System.out.println("Jwt-Authentication-Filter-Auth Header: "+authHeader);
        String username = null;
        String jwt = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        	System.out.println("Inside Auth Header Check");
            jwt = authHeader.substring(7); // Extract the token
            System.out.println("jwt:"+jwt);
            try {
                username = jwtUtil.extractUsername(jwt); // Extract username from token
                System.out.println("Username:"+username);
            } catch (ExpiredJwtException | SignatureException e) {
                // Handle exceptions accordingly (e.g., log them)
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return; // Stop processing if the token is invalid
            }
        }
        
     // If we found a token, validate it and set the authentication in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt)) {
                // Create the authentication token based on user roles
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
         // In JwtAuthenticationFilter
            if (userDetails != null) {
                System.out.println("Authenticated user: " + username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
    	            System.out.println("Authenticated user in jwt filter: " + auth.getName());
    	        }
                System.out.println("Security context set for user: " + username);
            }
        }
        
        chain.doFilter(request, response); // Continue with the filter chain
    }
}