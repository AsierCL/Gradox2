package com.example.gradox2.service.interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.gradox2.presentation.dto.admin.BannedUserResponse;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;

public interface IUserService {
    MyProfileResponse getCurrentUser();
    PublicProfileResponse getUserProfile(Long id);
    List<PublicProfileResponse> getAllUsers();
    Page<PublicProfileResponse> getUsersPaged(int page, int size, String sortBy);
    MyProfileResponse updateCurrentUser(UpdateMyProfileRequest userProfile);
    MyProfileResponse updateProfilePicture(MultipartFile file);
    MyProfileResponse deleteProfilePicture();
    void banUser(Long id);
    void banUser(Long id, String reason);
    void unbanUser(Long id);
    Page<BannedUserResponse> getBannedUsers(int page, int size);
}
