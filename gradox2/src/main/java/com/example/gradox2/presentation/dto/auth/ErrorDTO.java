package com.example.gradox2.presentation.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDTO {
    private String errorMessage;
    private String errorCode;
}
