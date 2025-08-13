package com.example.gradox2.service.implementation;

import java.security.Security;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;
import com.example.gradox2.security.JwtUtils;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.exceptions.UserNotFoundException;
import com.example.gradox2.service.interfaces.IUserService;
import com.example.gradox2.utils.mapper.DtoUserMapper;


@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public MyProfileResponse getCurrentUser() {
        // 1. Obtener el objeto de autenticación del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedAccessException("El usuario no está autenticado.");
        }

        // 2. Extraer el nombre de usuario del objeto de autenticación
        // El 'principal' puede ser un String o el objeto User que configuraste en JwtAuthFilter
        Object principal = authentication.getPrincipal();
        String username;

        // Aquí se asume que en tu JwtAuthFilter, el principal es un objeto User
        if (principal instanceof User) {
            username = ((User) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // 3. Buscar el usuario completo en la base de datos
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

        // 4. Mapear la entidad User a UserDTO
        // Usa tu DtoMapper para esto
        return DtoUserMapper.toProfileResponse(user);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthenticatedAccessException("El usuario no está autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new UnauthenticatedAccessException("El usuario no es una instancia de User.");
        }

        return (User) principal;
    }

    public PublicProfileResponse getUserProfile(Long id) {
        // 1. Buscar el usuario por ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // 2. Mapear la entidad User a PublicProfileResponse
        return DtoUserMapper.toPublicProfileResponse(user);
    }

    public List<PublicProfileResponse> getAllUsers() {
        // 1. Obtener todos los usuarios de la base de datos
        List<User> users = userRepository.findAll();

        // 2. Mapear cada usuario a PublicProfileResponse
        return users.stream()
                .map(DtoUserMapper::toPublicProfileResponse)
                .toList();
    }

    public MyProfileResponse updateCurrentUser(UpdateMyProfileRequest userProfile) {
        // 1. Obtener el usuario actual
        User user = getAuthenticatedUser();

        // 2. Actualizar los campos necesarios
        if (userProfile.getUsername() != null && !userProfile.getUsername().isBlank()) {
            user.setUsername(userProfile.getUsername());
        }

        if (userProfile.getPassword() != null && !userProfile.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(userProfile.getPassword()));
        }

        userRepository.save(user);
        MyProfileResponse updatedProfile = DtoUserMapper.toProfileResponse(user);
        return updatedProfile;
    }
}
