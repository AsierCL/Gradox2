package com.example.gradox2.service.interfaces;

import com.example.gradox2.presentation.dto.auth.AuthResponse;
import com.example.gradox2.presentation.dto.auth.PasswordResetConfirmRequest;
import com.example.gradox2.presentation.dto.auth.PasswordResetRequest;
import com.example.gradox2.presentation.dto.auth.LoginRequest;
import com.example.gradox2.presentation.dto.auth.RegisterRequest;
import com.example.gradox2.presentation.dto.auth.TokenRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse refreshToken(TokenRequest request);
    void logout(TokenRequest request);
    void requestPasswordReset(PasswordResetRequest request);
    void resetPassword(PasswordResetConfirmRequest request);
    boolean verifyToken(String token);
}
