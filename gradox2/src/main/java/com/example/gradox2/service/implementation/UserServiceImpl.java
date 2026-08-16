package com.example.gradox2.service.implementation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.ActionType;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.admin.BannedUserResponse;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.InvalidFileOperation;
import com.example.gradox2.service.exceptions.InvalidRoleOperationException;
import com.example.gradox2.service.exceptions.NotFoundException;
import com.example.gradox2.service.exceptions.AlreadyExistsException;
import com.example.gradox2.service.interfaces.FileUrlSigner;
import com.example.gradox2.service.interfaces.IAuditService;
import com.example.gradox2.service.interfaces.IUserService;
import com.example.gradox2.utils.GetAuthUser;
import com.example.gradox2.utils.SortUtils;
import com.example.gradox2.utils.mapper.UserMapper;


@Service
public class UserServiceImpl implements IUserService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final long MAX_PROFILE_PICTURE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "role",
            "reputation",
            "createdAt",
            "lastLogin",
            "enabled");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3StorageService s3StorageService;
    private final FileUrlSigner fileUrlSigner;
    private final ImageProcessingService imageProcessingService;
    private final IAuditService auditService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            S3StorageService s3StorageService, FileUrlSigner fileUrlSigner,
            ImageProcessingService imageProcessingService, IAuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3StorageService = s3StorageService;
        this.fileUrlSigner = fileUrlSigner;
        this.imageProcessingService = imageProcessingService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getCurrentUser() {
        User authUser = GetAuthUser.getAuthUser();

        // 3. Buscar el usuario completo en la base de datos
        User user = userRepository.findByUsername(authUser.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado en la base de datos"));

        // 4. Mapear la entidad User a UserDTO
        return UserMapper.mapper.toMyProfileResponse(user, fileUrlSigner);
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getUserProfile(Long id) {
        // 1. Buscar el usuario por ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        // 2. Mapear la entidad User a PublicProfileResponse
        return UserMapper.mapper.toPublicProfileResponse(user, fileUrlSigner);
    }

    @Transactional(readOnly = true)
    public List<PublicProfileResponse> getAllUsers() {
        return getUsersPaged(0, MAX_PAGE_SIZE, "id").getContent();
    }

    @Transactional(readOnly = true)
    public Page<PublicProfileResponse> getUsersPaged(int page, int size, String sortBy) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String safeSortBy = SortUtils.resolveSortBy(sortBy, "id", ALLOWED_SORT_FIELDS);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(safeSortBy).descending());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(user -> UserMapper.mapper.toPublicProfileResponse(user, fileUrlSigner));
    }

    @Transactional
    public MyProfileResponse updateCurrentUser(UpdateMyProfileRequest userProfile) {
        // 1. Obtener el usuario actual
        User user = GetAuthUser.getAuthUser();

        // 2. Actualizar los campos necesarios
        if (userProfile.getUsername() != null && !userProfile.getUsername().isBlank()) {
            if (!userProfile.getUsername().equals(user.getUsername())
                    && userRepository.findByUsername(userProfile.getUsername()).isPresent()) {
                throw new AlreadyExistsException("Username duplicado");
            }
            user.setUsername(userProfile.getUsername());
        }

        if (userProfile.getPassword() != null && !userProfile.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(userProfile.getPassword()));
        }

        user = userRepository.save(user);
        MyProfileResponse updatedProfile = UserMapper.mapper.toMyProfileResponse(user, fileUrlSigner);
        return updatedProfile;
    }

    @Transactional
    public MyProfileResponse updateProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileOperation("Debes adjuntar una imagen");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new InvalidFileOperation("Solo se permiten imágenes");
        }

        try {
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_PROFILE_PICTURE_BYTES) {
                throw new InvalidFileOperation("La imagen no puede superar los 5 MB");
            }

            byte[] webpBytes = imageProcessingService.processProfilePicture(bytes);

            User authUser = GetAuthUser.getAuthUser();
            User user = userRepository.findByUsername(authUser.getUsername())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado en la base de datos"));

            String oldKey = user.getProfilePictureKey();
            String newKey = s3StorageService.put(webpBytes, "image/webp");
            user.setProfilePictureKey(newKey);
            user = userRepository.save(user);

            if (oldKey != null && !oldKey.equals(newKey)) {
                s3StorageService.delete(oldKey);
            }

            return UserMapper.mapper.toMyProfileResponse(user, fileUrlSigner);
        } catch (IOException e) {
            throw new InternalServerErrorException("Error procesando la imagen", e);
        }
    }

    @Transactional
    public MyProfileResponse deleteProfilePicture() {
        User authUser = GetAuthUser.getAuthUser();
        User user = userRepository.findByUsername(authUser.getUsername())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado en la base de datos"));

        String oldKey = user.getProfilePictureKey();
        user.setProfilePictureKey(null);
        user = userRepository.save(user);

        if (oldKey != null) {
            s3StorageService.delete(oldKey);
        }

        return UserMapper.mapper.toMyProfileResponse(user, fileUrlSigner);
    }

    @Transactional
    public void banUser(Long id) {
        banUser(id, null);
    }

    @Transactional
    public void banUser(Long id, String reason) {
        User authUser = GetAuthUser.getAuthUser();

        if (authUser.getId().equals(id)) {
            throw new InvalidRoleOperationException("No puedes banearte a ti mismo. Usa una propuesta de expulsión.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getRole() == UserRole.MASTER) {
            throw new InvalidRoleOperationException(
                    "No puedes banear a un MASTER directamente. Crea una propuesta de expulsión.");
        }

        // Validar que el usuario no esté ya baneado
        if (!user.isEnabled()) {
            throw new AlreadyExistsException("El usuario ya está baneado.");
        }

        user.setEnabled(false);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        
        String details = "Usuario baneado" + (reason != null && !reason.isEmpty() ? ": " + reason : "");
        auditService.record(ActionType.BAN, "User", user.getId(), details);
    }

    @Transactional
    public void unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        
        // Validar que el usuario esté baneado
        if (user.isEnabled()) {
            throw new AlreadyExistsException("El usuario no está baneado.");
        }
        
        user.setEnabled(true);
        userRepository.save(user);
        auditService.record(ActionType.UNBAN, "User", user.getId(), "Usuario rehabilitado");
    }

    @Override
    public Page<BannedUserResponse> getBannedUsers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        return userRepository.findByEnabledFalse(pageable)
                .map(user -> new BannedUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getCreatedAt(),
                        user.getLastLogin()));
    }
}
