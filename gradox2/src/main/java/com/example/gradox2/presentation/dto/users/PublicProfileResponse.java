package com.example.gradox2.presentation.dto.users;

import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PublicProfileResponse {
    public String username;
    public String role;
    public Double reputation;
    public Set<String> badges;
}
