package com.example.gradox2.utils.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;

@Mapper
public interface UserMapper {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);


    // ------------------------------
    // User -> MyProfileResponse
    // ------------------------------
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "badges", source = "user", qualifiedByName = "mapBadges")
    MyProfileResponse toMyProfileResponse(User user);

    // ------------------------------
    // User -> PublicProfileResponse
    // ------------------------------
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "badges", source = "user", qualifiedByName = "mapBadges")
    PublicProfileResponse toPublicProfileResponse(User user);

    // ------------------------------
    // Método auxiliar para mapear badges a nombres
    // ------------------------------
    @Named("mapBadges")
    default Set<String> mapBadges(User user) {
        return user.getBadges()
                   .stream()
                   .map(badge -> badge.getName())
                   .collect(Collectors.toSet());
    }
}
