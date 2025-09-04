package com.example.gradox2.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterRequest {
    @Email(message = "Invalid email format")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@rai\\.usc\\.(es|gal)$",
        message = "El correo debe ser del dominio rai.usc.es o rai.usc.gal"
    )
    public String email;
    public String username;
    public String password;
}
