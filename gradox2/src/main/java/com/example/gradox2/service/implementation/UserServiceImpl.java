package com.example.gradox2.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.gradox2.presentation.dto.UserDTO;
import com.example.gradox2.service.interfaces.IUserService;


@Service
public class UserServiceImpl implements IUserService {

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        // Implementation logic to create a user
        return null;
    }

    @Override
    public List<UserDTO> findAllUsers() {
        // Implementation logic to find all users
        return null;
    }

    @Override
    public UserDTO findById(Long id) {
        // Implementation logic to find a user by ID
        return null;
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // Implementation logic to update a user
        return null;
    }

    @Override
    public String deleteUser(Long id) {
        // Implementation logic to delete a user
        return null;
    }
    
}
