package com.services.UserService.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.setAllowCredentials(true);
			config.addAllowedOrigin("*"); // Adjust as needed
			config.addAllowedHeader("*");
			config.addAllowedMethod("*");
			return config;
		})).authorizeHttpRequests(authorize -> authorize
//                .requestMatchers("/api/auth/**").permitAll() // Allow access to auth endpoints
				.requestMatchers("/api/users/**","/v2/api-docs","/v3/api-docs","/v3/api-docs/**","/swagger-resources","/swagger-resources/**","/configurtion-ui","/configuration-security","/swagger-ui/**","/webjars/**","/swagger-ui.html").permitAll().requestMatchers("/api/userProfile/**")
				.hasAnyAuthority("USER", "ADMIN")
//                .requestMatchers("/api/blogs/**").permitAll()// allow access to request from blog to this user service.
				.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}