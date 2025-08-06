package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.UserDTO;
import com.example.gradox2.service.interfaces.IUserService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private IUserService userService;
    
    // Create user
    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        /* return this.userService.createUser(userDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build()); */
        return new ResponseEntity<>(this.userService.createUser(userDTO), HttpStatus.CREATED);
    }

    // Find all users
    @GetMapping("/getAll")
    public ResponseEntity<List<UserDTO>> findAllUsers() {
        return new ResponseEntity<>(this.userService.findAllUsers(), HttpStatus.OK);
    }

    // Find user by ID
    @GetMapping("/get/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
        return new ResponseEntity<>(this.userService.findById(id), HttpStatus.OK);
    }
    
    // Update user
    @PostMapping("/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(this.userService.updateUser(id, userDTO), HttpStatus.OK);
    }

    // Delete user
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        return new ResponseEntity<>(this.userService.deleteUser(id), HttpStatus.NO_CONTENT);
    }
}
