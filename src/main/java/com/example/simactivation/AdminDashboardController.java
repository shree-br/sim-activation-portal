package com.example.simactivation;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final CustomerRepository customerRepository;

    public AdminDashboardController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats(
            HttpSession session) {

        Boolean loggedIn =
                (Boolean) session.getAttribute("adminLoggedIn");

        if (!Boolean.TRUE.equals(loggedIn)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long total = customerRepository.count();

        long pending = customerRepository
                .countByActivationStatusIgnoreCase("PENDING");

        long approved = customerRepository
                .countByActivationStatusIgnoreCase("APPROVED");

        long rejected = customerRepository
                .countByActivationStatusIgnoreCase("REJECTED");

        Map<String, Long> stats = Map.of(
                "total", total,
                "pending", pending,
                "approved", approved,
                "rejected", rejected
        );

        return ResponseEntity.ok(stats);
    }
}