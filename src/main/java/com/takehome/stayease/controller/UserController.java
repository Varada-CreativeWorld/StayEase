package com.takehome.stayease.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.takehome.stayease.dto.AuthResponse;
import com.takehome.stayease.dto.UserLoginRequest;
import com.takehome.stayease.dto.UserRegisterRequest;
import com.takehome.stayease.dto.UserResponse;
import com.takehome.stayease.entity.Role;
import com.takehome.stayease.security.JwtTokenProvider;
import com.takehome.stayease.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody UserRegisterRequest userRequest) {
        logger.info("Registering user with email: {}", userRequest.getEmail());

        if (userRequest.getRole() == null || userRequest.getRole().isBlank()) {
            userRequest.setRole("CUSTOMER");
        } else {
            Role roleEnum = Role.valueOf(userRequest.getRole().toUpperCase());
            userRequest.setRole(roleEnum.name());
        }

        AuthResponse authResponse = userService.registerUser(userRequest);
        logger.info("User registered successfully: {}", authResponse);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody UserLoginRequest userRequest) {
        logger.info("User attempting to log in with email: {}", userRequest.getEmail());

        try {
            AuthResponse authResponse = userService.loginUser(userRequest.getEmail(), userRequest.getPassword());
            logger.info("User logged in successfully: {}", userRequest.getEmail());
            return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            logger.warn("Login failed for user: {}", userRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Invalid email or password"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpServletRequest request) {
        logger.info("Fetching user with ID: {}", id);

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Authorization token is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is missing or invalid");
        }

        String token = authorizationHeader.substring(7);

        // Check if the token is valid
        if (!jwtTokenProvider.validateToken(token)) {
            logger.warn("Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }

        UserResponse userResponse = userService.getUserById(id);
        String email = jwtTokenProvider.getUsername(token);
        logger.debug("User email from token: {}", email);

        if (!userResponse.getEmail().equals(email)) {
            logger.warn("Access denied for user ID: {}", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: Only LoggedIn user can see their data");
        }

        logger.info("User data retrieved successfully for user ID: {}", id);
        return ResponseEntity.ok(userResponse);
    }
}
