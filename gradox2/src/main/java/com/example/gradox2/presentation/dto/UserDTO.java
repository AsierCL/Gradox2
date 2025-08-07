package com.example.gradox2.presentation.dto;

import com.example.gradox2.persistence.entities.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


////////////////////  MODIFICAR PARA QUITAR GETTERS Y SETTERS  ////////////////////
@NoArgsConstructor
@AllArgsConstructor
@Data 
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private UserRole userRole;
}
