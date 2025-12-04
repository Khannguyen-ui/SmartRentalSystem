package com.smartrental.backend.controller;

import com.smartrental.backend.entity.Transaction;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.TransactionRepository;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Xem lịch sử biến động số dư
    @GetMapping("/my-history")
    public ResponseEntity<List<Transaction>> getMyHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return ResponseEntity.ok(transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }
}