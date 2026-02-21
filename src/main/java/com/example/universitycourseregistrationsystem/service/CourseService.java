package com.example.universitycourseregistrationsystem.service;

import com.example.universitycourseregistrationsystem.domain.Course;
import com.example.universitycourseregistrationsystem.domain.Role;
import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.CourseRequest;
import com.example.universitycourseregistrationsystem.repository.CourseRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;

    public CourseService(CourseRepository courseRepository, UserService userService) {
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getTeacherCourses(Long teacherId) {
        return courseRepository.findAllByTeacherId(teacherId);
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        User teacher = userService.requireRole(Role.TEACHER);
        courseRepository.findByCourseNo(request.courseNo()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course number already exists");
        });
        Course course = new Course();
        course.setCourseNo(request.courseNo());
        course.setCourseName(request.courseName());
        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, CourseRequest request) {
        User teacher = userService.requireRole(Role.TEACHER);
        Course course = getCourseOrThrow(courseId);
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course");
        }
        if (!course.getCourseNo().equals(request.courseNo())) {
            courseRepository.findByCourseNo(request.courseNo()).ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Course number already exists");
            });
        }
        course.setCourseNo(request.courseNo());
        course.setCourseName(request.courseName());
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        User teacher = userService.requireRole(Role.TEACHER);
        Course course = getCourseOrThrow(courseId);
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your course");
        }
        courseRepository.delete(course);
    }

    public Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }
}
