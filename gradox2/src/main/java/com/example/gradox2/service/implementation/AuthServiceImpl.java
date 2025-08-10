package com.example.gradox2.service.implementation;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.auth.AuthResponse;
import com.example.gradox2.presentation.dto.auth.LoginRequest;
import com.example.gradox2.presentation.dto.auth.RegisterRequest;
import com.example.gradox2.security.JwtUtils;
import com.example.gradox2.service.exceptions.UserAlreadyExistsException;
import com.example.gradox2.service.interfaces.IAuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtUtils.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username)) {
            throw new UserAlreadyExistsException("Usuario ya existe");
        }

        User user = new User();
        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setPasswordHash(passwordEncoder.encode(request.password));
        user.setRole(UserRole.USER); // Default role

        userRepository.save(user);
        String token = jwtUtils.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
