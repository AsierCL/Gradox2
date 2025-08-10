package com.example.gradox2.presentation.dto.users;


import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateMyProfileRequest {
    public String username;
    public String password;
}
