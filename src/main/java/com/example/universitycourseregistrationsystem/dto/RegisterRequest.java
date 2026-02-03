package com.example.universitycourseregistrationsystem.dto;

import com.example.universitycourseregistrationsystem.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(min = 6, max = 64) String password,
        @NotNull Role role
) {
}
