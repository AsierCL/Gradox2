package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;

public interface IUserService {
    MyProfileResponse getCurrentUser();
    PublicProfileResponse getUserProfile(Long id);
    List<PublicProfileResponse> getAllUsers();
    MyProfileResponse updateCurrentUser(UpdateMyProfileRequest userProfile);
}
