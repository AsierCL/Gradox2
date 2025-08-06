package com.example.gradox2.service.interfaces;

import java.util.List;

import com.example.gradox2.presentation.dto.UserDTO;

public interface IUserService {
    UserDTO createUser(UserDTO userDTO);
    List<UserDTO> findAllUsers();
    UserDTO findById(Long id);
    UserDTO updateUser(Long id, UserDTO userDTO);
    String deleteUser(Long id);
}
