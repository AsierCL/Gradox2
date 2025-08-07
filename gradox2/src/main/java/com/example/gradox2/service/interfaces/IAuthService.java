package com.example.gradox2.service.interfaces;

import com.example.gradox2.presentation.dto.AuthResponse;
import com.example.gradox2.presentation.dto.LoginRequest;
import com.example.gradox2.presentation.dto.RegisterRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}