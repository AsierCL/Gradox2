package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.interfaces.IUserService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.mapper.UserMapper;


@Service
public class UserServiceImpl implements IUserService {
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getCurrentUser() {
        User authUser = GetAuthUser.getAuthUser();

        // 3. Buscar el usuario completo en la base de datos
        User user = userRepository.findByUsername(authUser.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado en la base de datos"));

        // 4. Mapear la entidad User a UserDTO
        return UserMapper.mapper.toMyProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getUserProfile(Long id) {
        // 1. Buscar el usuario por ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 2. Mapear la entidad User a PublicProfileResponse
        return UserMapper.mapper.toPublicProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<PublicProfileResponse> getAllUsers() {
        return getUsersPaged(0, MAX_PAGE_SIZE, "id").getContent();
    }

    @Transactional(readOnly = true)
    public Page<PublicProfileResponse> getUsersPaged(int page, int size, String sortBy) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(sortBy).descending());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserMapper.mapper::toPublicProfileResponse);
    }

    @Transactional
    public MyProfileResponse updateCurrentUser(UpdateMyProfileRequest userProfile) {
        // 1. Obtener el usuario actual
        User user = GetAuthUser.getAuthUser();

        // 2. Actualizar los campos necesarios
        if (userProfile.getUsername() != null && !userProfile.getUsername().isBlank()) {
            if (userRepository.findByUsername(userProfile.getUsername()).isPresent()) {
                throw new AlreadyExistsException("Username duplicado");
            }
            user.setUsername(userProfile.getUsername());
        }

        if (userProfile.getPassword() != null && !userProfile.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(userProfile.getPassword()));
        }

        userRepository.save(user);
        MyProfileResponse updatedProfile = UserMapper.mapper.toMyProfileResponse(user);
        return updatedProfile;
    }

    @Transactional
    public void banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
