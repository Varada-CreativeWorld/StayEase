package com.takehome.stayease.dto;

import com.takehome.stayease.entity.Role;
import com.takehome.stayease.entity.User;
import com.takehome.stayease.validation.ValidEmail;
import com.takehome.stayease.validation.ValidRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequest {

    @NotBlank(message = "First Name is mandatory")
    private String firstName;

    @NotBlank(message = "Last Name is mandatory")
    private String lastName;
    
    @NotBlank(message = "Email is mandatory")
    @ValidEmail(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password should be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    @ValidRole(message = "Invalid role. Accepted values are: CUSTOMER, HOTEL_MANAGER, ADMIN")
    private String role; // Role is optional now

    public User toUser() {
        User user = new User();
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        
        // Convert role string to Role enum
        if (this.role != null) {
            try {
                user.setRole(Role.valueOf(this.role.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Handle the case where the role string does not match any Role enum value
                throw new IllegalArgumentException("Invalid role: " + this.role + ". Accepted values are: CUSTOMER, HOTEL_MANAGER, ADMIN.");
            }
        } else {
            user.setRole(Role.CUSTOMER); // Set default role if not provided
        }
    
        return user;
    }
}
