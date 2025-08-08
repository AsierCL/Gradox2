package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.ProfileResponse;
import com.example.gradox2.service.interfaces.IUserService;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/users")
public class UserController {
    
    private final IUserService userService;
    
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUser() {
        ProfileResponse user = userService.getCurrentUser();
        System.out.println("getCurrentUser called");
        //ProfileResponse user = new ProfileResponse();
        user.setUsername("Asier");
        return ResponseEntity.ok(user);
    }
 
}
