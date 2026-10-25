package com.example.simactivation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {

    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String storedPasswordHash;

    public AdminAuthController(
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.password-hash}") String storedPasswordHash) {

        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.storedPasswordHash =
                storedPasswordHash == null ? "" : storedPasswordHash.trim();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        if (request == null
                || request.getUsername() == null
                || request.getPassword() == null) {

            return ResponseEntity.badRequest()
                    .body(new LoginResponse(
                            "Username and password are required"));
        }

        String username = request.getUsername();
        String password = request.getPassword();

        if (adminUsername.equals(username)
                && passwordEncoder.matches(
                        password, storedPasswordHash)) {

            session.setAttribute("adminLoggedIn", true);

            return ResponseEntity.ok(
                    new LoginResponse("Login successful"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(
                        "Invalid username or password"));
    }

    @GetMapping("/session")
    public ResponseEntity<LoginResponse> checkSession(
            HttpSession session) {

        Boolean loggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (Boolean.TRUE.equals(loggedIn)) {
            return ResponseEntity.ok(
                    new LoginResponse("Admin is logged in"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse("Please login first"));
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                new LoginResponse("Logged out successfully"));
    }
}