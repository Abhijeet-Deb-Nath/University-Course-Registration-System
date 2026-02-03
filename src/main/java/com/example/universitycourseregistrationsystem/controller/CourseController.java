package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.CourseRequest;
import com.example.universitycourseregistrationsystem.dto.CourseResponse;
import com.example.universitycourseregistrationsystem.dto.RegistrationResponse;
import com.example.universitycourseregistrationsystem.service.CourseService;
import com.example.universitycourseregistrationsystem.service.RegistrationService;
import com.example.universitycourseregistrationsystem.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final UserService userService;

    public CourseController(
            CourseService courseService,
            RegistrationService registrationService,
            UserService userService
    ) {
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.userService = userService;
    }

    @GetMapping
    public List<CourseResponse> listAll() {
        return courseService.getAllCourses().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('TEACHER')")
    public List<CourseResponse> listMine() {
        User teacher = userService.getCurrentUser();
        return courseService.getTeacherCourses(teacher.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest request) {
        return toResponse(courseService.createCourse(request));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public CourseResponse update(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request
    ) {
        return toResponse(courseService.updateCourse(courseId, request));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
    }

    @GetMapping("/{courseId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    public List<RegistrationResponse> students(@PathVariable Long courseId) {
        return registrationService.getRegistrationsForCourse(courseId).stream()
                .map(this::toRegistrationResponse)
                .collect(Collectors.toList());
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseNo(),
                course.getCourseName(),
                course.getTeacher().getId(),
                course.getTeacher().getUsername()
        );
    }

    private RegistrationResponse toRegistrationResponse(
            com.example.universitycourseregistrationsystem.domain.Registration registration
    ) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getCourse().getId(),
                registration.getCourse().getCourseNo(),
                registration.getCourse().getCourseName(),
                registration.getStudent().getId(),
                registration.getStudent().getUsername()
        );
    }
}
