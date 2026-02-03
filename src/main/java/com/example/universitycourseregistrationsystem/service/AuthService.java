package com.example.universitycourseregistrationsystem.service;

import com.example.universitycourseregistrationsystem.domain.User;
import com.example.universitycourseregistrationsystem.dto.AuthRequest;
import com.example.universitycourseregistrationsystem.dto.AuthResponse;
import com.example.universitycourseregistrationsystem.security.JwtService;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userService.findByUsername(authentication.getName()).orElseThrow();
        String token = jwtService.generateToken(user.getUsername(), Map.of("role", user.getRole().name()));
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
