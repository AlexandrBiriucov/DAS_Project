package com.faf.jiggly.pocket.adapter.in.http;

import com.faf.jiggly.pocket.domain.usecase.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public record RegisterRequest(String email, String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.email() == null || !request.email().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email"));
        }
        if (request.password() == null || request.password().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters"));
        }
        try {
            var user = userService.register(request.email(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", user.id().asString()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}