package com.takehome.stayease.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.takehome.stayease.dto.AuthResponse;
import com.takehome.stayease.dto.UserRegisterRequest;
import com.takehome.stayease.dto.UserResponse;
import com.takehome.stayease.entity.Booking;
import com.takehome.stayease.entity.User;
import com.takehome.stayease.exception.UserAlreadyExistsException;
import com.takehome.stayease.exception.UserNotFoundException;
import com.takehome.stayease.repository.UserRepository;
import com.takehome.stayease.security.JwtTokenProvider;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse registerUser(UserRegisterRequest userRequest) {
        logger.info("Registering user with email: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            logger.error("User with email {} already exists.", userRequest.getEmail());
            throw new UserAlreadyExistsException("User with email " + userRequest.getEmail() + " already exists.");
        }

        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        User user = userRequest.toUser();

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.createToken(savedUser.getEmail(), savedUser.getRole());

        logger.info("User registered successfully with email: {}", savedUser.getEmail());
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse loginUser(String email, String password) {
        logger.info("Attempting to login user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.error("Invalid credentials for email: {}", email);
            throw new UserNotFoundException("Invalid credentials for email: " + email);
        }

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        logger.info("User logged in successfully with email: {}", email);
        return new AuthResponse(token);
    }

    @Override
    public UserResponse getUserById(Long id) {
        logger.info("Retrieving user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("User not found with ID: {}", id);
                return new UserNotFoundException("User not found with ID: " + id);
            });

        UserResponse response = convertToUserResponse(user);
        logger.info("User retrieved successfully with ID: {}", id);
        return response;
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());

        List<Long> bookingIds = (user.getBookings() != null) ?
            user.getBookings().stream()
                .map(Booking::getId)
                .collect(Collectors.toList()) :
            new ArrayList<>();
        dto.setBookingIDs(bookingIds);

        return dto;
    }
}
