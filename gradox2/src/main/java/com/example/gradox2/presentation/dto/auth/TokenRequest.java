package com.example.gradox2.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TokenRequest {
    @NotBlank(message = "Token is required")
    private String refreshToken;
}
