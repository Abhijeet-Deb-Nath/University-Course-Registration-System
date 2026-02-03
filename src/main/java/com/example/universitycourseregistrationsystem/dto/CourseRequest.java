package com.example.universitycourseregistrationsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank @Size(max = 32) String courseNo,
        @NotBlank @Size(max = 120) String courseName
) {
}
