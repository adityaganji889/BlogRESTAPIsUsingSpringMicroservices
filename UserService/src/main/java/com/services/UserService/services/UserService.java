package com.services.UserService.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
//import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.services.UserService.dtos.MailBody;
//import com.services.UserService.controllers.Random;
import com.services.UserService.dtos.UserRegistrationRequest;
import com.services.UserService.entities.Otp;
import com.services.UserService.entities.Role;
import com.services.UserService.entities.User;
import com.services.UserService.repositories.OtpRepository;
import com.services.UserService.repositories.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OtpRepository otpRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Async("asyncTaskExecutor") // To make this method make asynchronous calls to mysql db
	public CompletableFuture<Void> registerNewUser(UserRegistrationRequest request) {
		return CompletableFuture.runAsync(() -> {
			try {
				Optional<User> userExists = userRepository.findByUsername(request.getUsername());
				if (!userExists.isPresent()) {
					User user = new User();
					user.setUsername(request.getUsername());
					user.setPassword(passwordEncoder.encode(request.getPassword()));
//		            user.setRoles(Collections.singleton(request.getRole())); // Set role using enum
					if (request.getRole() == Role.USER) {
						user.setRoles(Role.USER);
					} else {
						user.setRoles(Role.ADMIN);
					}
					userRepository.save(user);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<Optional<User>> findByUsername(String username) {
		return CompletableFuture.supplyAsync(() -> userRepository.findByUsername(username));
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<Optional<Otp>> findByOtpAndUser(Integer otpValue, User user) {
		return CompletableFuture.supplyAsync(() -> otpRepository.findByOtpAndUser(otpValue, user));
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<User> getUserInfo() {
		// Get the current authentication from the security context

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			System.out.println("Authenticated user in service: " + authentication.getName());
		}

		// Ensure the principal is of type UserDetails
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		// Create a callable that maintains the security context
		Callable<User> callable = new DelegatingSecurityContextCallable<>(() -> {
			var userInfo = userRepository.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

			// Map userInfo to User entity
			User user = new User();
			user.setId(userInfo.getId());
			user.setUsername(userInfo.getUsername());
			user.setPassword(userInfo.getPassword());
			user.setRoles(userInfo.getRoles());

			return user;
		});

		// Use CompletableFuture to execute the callable and maintain the security
		// context
		return CompletableFuture.supplyAsync(() -> {
			try {
//	        	SecurityContextHolder.setContext(originalContext);
				return callable.call(); // This call retains the security context
			} catch (Exception e) {
				e.printStackTrace();
				return null; // Handle exception as needed
			}
		});

	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<User> getUserInfoById(Long id) {
		// Create a callable that maintains the security context
		Callable<User> callable = new DelegatingSecurityContextCallable<>(() -> {
			Optional<User> user = userRepository.findById(id);
			return user.orElse(null); // Return user or null if not found
		});

		// Use CompletableFuture to execute the callable
		return CompletableFuture.supplyAsync(() -> {
			try {
				return callable.call(); // This call retains the security context
			} catch (Exception e) {
				e.printStackTrace();
				return null; // Handle exception as needed
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<Void> deleteUserById(Long id) {
		return CompletableFuture.runAsync(() -> {
			Optional<User> user = userRepository.findById(id);
			if (user.isPresent()) {
				userRepository.deleteById(user.get().getId());
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<Void> deleteOtpById(Integer id) {
		return CompletableFuture.runAsync(() -> {
			Optional<Otp> otp = otpRepository.findById(id);
			if (otp.isPresent()) {
				otpRepository.deleteById(otp.get().getFpid());
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<User> updateUserRoleToAdminById(Long id) {
		CompletableFuture<User> cf = getUserInfo();
		return CompletableFuture.supplyAsync(() -> {
			try {
				Optional<User> user = userRepository.findById(id);
				User user1 = null;
				try {
					user1 = cf.get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (user.isPresent()) {
					User user2 = user.get();
					if (user2.getId() != user1.getId() && user2.getRoles().equals(Role.USER)) {
						user2.setRoles(Role.ADMIN);
						User updatedUser = userRepository.save(user2);
						return updatedUser;
					} else {
						return user2;
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<User> updateUserRoleToUserById(Long id) {
		CompletableFuture<User> cf = getUserInfo();
		return CompletableFuture.supplyAsync(() -> {
			try {
				Optional<User> user = userRepository.findById(id);
				User user1 = null;
				try {
					user1 = cf.get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (user.isPresent()) {
					User user2 = user.get();
					if (user2.getId() != user1.getId() && user2.getRoles().equals(Role.ADMIN)) {
						user2.setRoles(Role.USER);
						User updatedUser = userRepository.save(user2);
						return updatedUser;
					} else {
						return user2;
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<List<User>> getAllUsers() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				List<User> users = userRepository.findAll();
				return users.isEmpty() ? null : users;
			} catch (Exception e) {
				e.printStackTrace();
				return null; // Or complete the future exceptionally
			}
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<Otp> verifyEmailSend(String email, User user) {
		return CompletableFuture.supplyAsync(() -> {
			int otp = otpGenerator();
			MailBody mailBody = MailBody.builder().to(email)
					.text("This is the OTP for your Forgot Password request : " + otp +" is valid till next 10 minutes.")
					.subject("OTP for Forgot Password request").build();

			Otp fp = Otp.builder().otpValue(otp).expirationTime(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
					.user(user).build();

			emailService.sendSimpleMessage(mailBody);
			Otp newOtp = otpRepository.save(fp);
			return newOtp;
		});
	}

	@Async("asyncTaskExecutor")
	public CompletableFuture<Boolean> changePasswordHandle(String email, String newPassword, String repeatPassword) {
		return CompletableFuture.supplyAsync(() -> {
			Boolean isPasswordMatched = false;
			if (!newPassword.equals(repeatPassword)) {
				return isPasswordMatched;
			} else {
				isPasswordMatched = true;
			}

			String encodedPassword = passwordEncoder.encode(newPassword);
			userRepository.updatePassword(email, encodedPassword);
			return isPasswordMatched;
		});
	}

	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

	private Integer otpGenerator() {
		Random random = new Random();
		return random.nextInt(100_000, 999_999);
	}

}