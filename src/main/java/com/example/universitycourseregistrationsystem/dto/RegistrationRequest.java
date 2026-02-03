package com.example.universitycourseregistrationsystem.dto;

import jakarta.validation.constraints.NotNull;

public record RegistrationRequest(
        @NotNull Long courseId
) {
}
