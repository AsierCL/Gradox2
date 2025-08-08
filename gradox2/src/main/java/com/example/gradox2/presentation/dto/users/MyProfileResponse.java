package com.example.gradox2.presentation.dto.users;

import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MyProfileResponse {
    public Long id;
    public String username;
    public String email;
    public String role;
    public Double reputation;
    public Instant createdAt;
    public Instant lastLogin;
    public Set<String> badges; // Nombres de las insignias
}