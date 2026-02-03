package com.example.universitycourseregistrationsystem.repository;

import com.example.universitycourseregistrationsystem.domain.Registration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Registration> findAllByStudentId(Long studentId);
    List<Registration> findAllByCourseId(Long courseId);
    Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
