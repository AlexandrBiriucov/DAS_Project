package com.faf.jiggly.pocket.adapter.in.http;

import com.faf.jiggly.pocket.domain.usecase.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
 

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



    
    public record LoginRequest(String email, String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        if (request.email() == null || request.password() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password required"));
        }
        var user = userService.authenticate(request.email(), request.password());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }

        var oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        var session = httpRequest.getSession(true);
        session.setAttribute("userId", user.get().id().asString());

        return ResponseEntity.ok(Map.of("id", user.get().id().asString()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        var session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest httpRequest) {
        var session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }
        return ResponseEntity.ok(Map.of("id", session.getAttribute("userId")));
    }



}

   