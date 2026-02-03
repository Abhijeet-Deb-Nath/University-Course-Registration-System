package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.dto.RegistrationRequest;
import com.example.universitycourseregistrationsystem.dto.RegistrationResponse;
import com.example.universitycourseregistrationsystem.service.RegistrationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody RegistrationRequest request) {
        return toResponse(registrationService.register(request.courseId()));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void drop(@Valid @RequestBody RegistrationRequest request) {
        registrationService.drop(request.courseId());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('STUDENT')")
    public List<RegistrationResponse> myRegistrations() {
        return registrationService.getMyRegistrations().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RegistrationResponse toResponse(
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
