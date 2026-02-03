package com.example.universitycourseregistrationsystem.dto;

public record RegistrationResponse(
        Long id,
        Long courseId,
        String courseNo,
        String courseName,
        Long studentId,
        String studentUsername
) {
}
