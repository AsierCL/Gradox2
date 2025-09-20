package com.example.gradox2.service.implementation;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.VerificationToken;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.presentation.dto.auth.AuthResponse;
import com.example.gradox2.presentation.dto.auth.LoginRequest;
import com.example.gradox2.presentation.dto.auth.RegisterRequest;
import com.example.gradox2.security.JwtUtils;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.interfaces.IAuthService;
import com.example.gradox2.utils.EmailService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            EmailService emailService, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new UnauthenticatedAccessException("Contraseña incorrecta");
        }

        if (!user.isEnabled()) {
            throw new UnauthenticatedAccessException("Usuario no verificado");
        }

        String token = jwtUtils.generateToken(user);
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username)) {
            throw new AlreadyExistsException("Usuario ya existe");
        }

        User user = new User();
        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setPasswordHash(passwordEncoder.encode(request.password));
        user.setEnabled(false); // Require email verification
        user.setRole(UserRole.USER); // Default role
        userRepository.save(user);

        String token = java.util.UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken(token, user);
        verificationTokenRepository.save(vt);

        String verifyUrl = "http://localhost:8080/api/auth/verify?token=" + token;

        emailService.sendEmail(user.getEmail(), "Verifica tu cuenta",
                "Haz clic aquí para verificar tu cuenta: " + verifyUrl);

        System.out.println(user.getUsername() +  "Haz clic aquí para verificar tu cuenta: " + verifyUrl);

        return new AuthResponse(null, user.getUsername(), user.getRole().name());
    }

    @Override
    public boolean verifyToken(String token) {
        return verificationTokenRepository.findByToken(token).map(vt -> {
            if (vt.isExpired()) {
                verificationTokenRepository.delete(vt);
                return false;
            }
            User user = vt.getUser();
            user.setEnabled(true);
            userRepository.save(user);
            verificationTokenRepository.delete(vt);
            return true;
        }).orElse(false);
    }
}
