package com.example.universitycourseregistrationsystem.repository;

import com.example.universitycourseregistrationsystem.domain.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByTeacherId(Long teacherId);
    Optional<Course> findByCourseNo(String courseNo);
}
