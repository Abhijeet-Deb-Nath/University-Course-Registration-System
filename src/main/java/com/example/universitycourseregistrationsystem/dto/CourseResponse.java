package com.example.universitycourseregistrationsystem.dto;

public record CourseResponse(
        Long id,
        String courseNo,
        String courseName,
        Long teacherId,
        String teacherUsername
) {
}
