package com.example.universitycourseregistrationsystem.controller;

import com.example.universitycourseregistrationsystem.dto.AuthRequest;
import com.example.universitycourseregistrationsystem.dto.AuthResponse;
import com.example.universitycourseregistrationsystem.dto.RegisterRequest;
import com.example.universitycourseregistrationsystem.dto.UserSummary;
import com.example.universitycourseregistrationsystem.service.AuthService;
import com.example.universitycourseregistrationsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummary register(@Valid @RequestBody RegisterRequest request) {
        return toSummary(userService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    private UserSummary toSummary(com.example.universitycourseregistrationsystem.domain.User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getRole().name());
    }
}
