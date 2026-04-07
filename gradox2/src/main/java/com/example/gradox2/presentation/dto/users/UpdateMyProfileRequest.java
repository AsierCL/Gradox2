package com.example.gradox2.presentation.dto.users;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateMyProfileRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    public String username;

    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    public String password;
}
