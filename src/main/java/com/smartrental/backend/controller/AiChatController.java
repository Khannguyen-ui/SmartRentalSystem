package com.smartrental.backend.controller;

import com.smartrental.backend.service.impl.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final GeminiService geminiService;
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập câu hỏi!"));
        }

        String answer = geminiService.getConsultation(question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}