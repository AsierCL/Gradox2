package com.example.gradox2.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.utils.GetAuthUser;

@RestController
@RequestMapping("/health")
public class HealthController {

    @RequestMapping
    public ResponseEntity<String> checkHealth() {
        User user = GetAuthUser.getAuthUserUnsecureEndpoint();
        if(user == null){
            return ResponseEntity.ok("Service is up and running! (Unauthenticated)");
        }else{
            return ResponseEntity.ok("Service is up and running! (Authenticated as " + user.getUsername() + ")");
        }
    }
}
