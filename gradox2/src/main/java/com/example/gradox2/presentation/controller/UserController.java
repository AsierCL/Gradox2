package com.example.gradox2.presentation.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.presentation.dto.users.MyProfileResponse;
import com.example.gradox2.presentation.dto.users.PublicProfileResponse;
import com.example.gradox2.presentation.dto.users.UpdateMyProfileRequest;
import com.example.gradox2.service.interfaces.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/users")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> getCurrentUser() {
        MyProfileResponse user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<MyProfileResponse> updateCurrentUser(@RequestBody UpdateMyProfileRequest updateMyProfileRequest) {
        MyProfileResponse updatedUser = userService.updateCurrentUser(updateMyProfileRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicProfileResponse> getUserProfile(@PathVariable Long id) {
        PublicProfileResponse userProfile = userService.getUserProfile(id);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PublicProfileResponse>> getAllUsers() {
        List<PublicProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<PublicProfileResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            //@RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Page<PublicProfileResponse> users = userService.getUsersPaged(page, 5, sortBy);
        return ResponseEntity.ok(users);
    }

}
