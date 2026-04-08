package com.example.gradox2.service.implementation;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.RefreshToken;
import com.example.gradox2.persistence.entities.PasswordResetToken;
import com.example.gradox2.persistence.entities.VerificationToken;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.persistence.repository.RefreshTokenRepository;
import com.example.gradox2.persistence.repository.PasswordResetTokenRepository;
import com.example.gradox2.persistence.repository.VerificationTokenRepository;
import com.example.gradox2.presentation.dto.auth.AuthResponse;
import com.example.gradox2.presentation.dto.auth.PasswordResetConfirmRequest;
import com.example.gradox2.presentation.dto.auth.PasswordResetRequest;
import com.example.gradox2.presentation.dto.auth.LoginRequest;
import com.example.gradox2.presentation.dto.auth.RegisterRequest;
import com.example.gradox2.presentation.dto.auth.TokenRequest;
import com.example.gradox2.security.JwtUtils;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.interfaces.IAuthService;
import com.example.gradox2.utils.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Environment environment;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshExpirationMs;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils,
            EmailService emailService, VerificationTokenRepository verificationTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.environment = environment;
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

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthenticatedAccessException("Refresh token inválido"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);
            throw new UnauthenticatedAccessException("Refresh token inválido o expirado");
        }

        User user = storedToken.getUser();
        if (!user.isEnabled()) {
            throw new UnauthenticatedAccessException("Usuario no verificado o deshabilitado");
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(TokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthenticatedAccessException("Refresh token inválido"));

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plus(24, ChronoUnit.HOURS))
                    .build();

            passwordResetTokenRepository.save(resetToken);

            String resetUrl = appBaseUrl + "/api/auth/password/reset?token=" + token;
            emailService.sendEmail(user.getEmail(), "Restablecer contraseña",
                    "Usa este token para restablecer tu contraseña: " + resetUrl);
        });
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new UnauthenticatedAccessException("Token inválido o expirado"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new UnauthenticatedAccessException("Token inválido o expirado");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username)) {
            throw new AlreadyExistsException("Usuario ya existe");
        }

        if (userRepository.existsByEmail(request.email)) {
            throw new AlreadyExistsException("Email ya existe");
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

        String verifyUrl = appBaseUrl + "/api/auth/verify?token=" + token;

        emailService.sendEmail(user.getEmail(), "Verifica tu cuenta",
                "Haz clic aquí para verificar tu cuenta: " + verifyUrl);

        if (isLocalOrDemo()) {
            System.out.println(user.getUsername() + " Haz clic aquí para verificar tu cuenta: " + verifyUrl);
        } else {
            logger.info("Verification email sent to {}", user.getEmail());
        }

        return new AuthResponse(null, null, user.getUsername(), user.getRole().name());
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

    private boolean isLocalOrDemo() {
        for (String profile : environment.getActiveProfiles()) {
            if ("local".equals(profile) || "demo".equals(profile)) {
                return true;
            }
        }
        return false;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtils.generateToken(user);
        String refreshTokenValue = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshTokenValue, user.getUsername(), user.getRole().name());
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
            .expiryDate(LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
