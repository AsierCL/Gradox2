package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;

public interface IUserService {
    MyProfileResponse getCurrentUser();
    PublicProfileResponse getUserProfile(Long id);
}
