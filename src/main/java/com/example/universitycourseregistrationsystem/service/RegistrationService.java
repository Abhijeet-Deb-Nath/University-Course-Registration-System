package com.example.universitycourseregistrationsystem.service;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Registration;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.repository.RegistrationRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final CourseService courseService;
    private final UserService userService;

    public RegistrationService(
            RegistrationRepository registrationRepository,
            CourseService courseService,
            UserService userService
    ) {
        this.registrationRepository = registrationRepository;
        this.courseService = courseService;
        this.userService = userService;
    }

    @Transactional
    public Registration register(Long courseId) {
        User student = userService.requireRole(Role.STUDENT);
        Course course = courseService.getCourseOrThrow(courseId);
        if (registrationRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered");
        }
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        return registrationRepository.save(registration);
    }

    @Transactional
    public void drop(Long courseId) {
        User student = userService.requireRole(Role.STUDENT);
        Registration registration = registrationRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        registrationRepository.delete(registration);
    }

    public List<Registration> getMyRegistrations() {
        User student = userService.requireRole(Role.STUDENT);
        return registrationRepository.findAllByStudentId(student.getId());
    }

    public List<Registration> getRegistrationsForCourse(Long courseId) {
        User teacher = userService.requireRole(Role.TEACHER);
        Course course = courseService.getCourseOrThrow(courseId);
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course");
        }
        return registrationRepository.findAllByCourseId(courseId);
    }
}
