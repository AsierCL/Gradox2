package com.example.gradox2.utils.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;

@Mapper
public interface UserMapper {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);

    MyProfileResponse toMyProfileResponse(User user);

    PublicProfileResponse toPublicProfileResponse(User user);
}
