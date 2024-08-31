package com.takehome.stayease.service;

import com.takehome.stayease.dto.AuthResponse;
import com.takehome.stayease.dto.UserRegisterRequest;
import com.takehome.stayease.dto.UserResponse;
import com.takehome.stayease.exception.UserNotFoundException;

public interface UserService {
    AuthResponse registerUser(UserRegisterRequest userRequest);
    AuthResponse loginUser(String email, String password) throws UserNotFoundException;
    UserResponse getUserById(Long id);
}
