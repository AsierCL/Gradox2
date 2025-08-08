package com.example.gradox2.service.implementation;

import java.security.Security;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;
import com.example.gradox2.service.exceptions.UserNotFoundException;
import com.example.gradox2.service.interfaces.IUserService;
import com.example.gradox2.utils.mapper.DtoUserMapper;


@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    
    public PublicProfileResponse getUserProfile(Long id) {
        // 1. Buscar el usuario por ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // 2. Mapear la entidad User a PublicProfileResponse
        return DtoUserMapper.toPublicProfileResponse(user);
    }
}
