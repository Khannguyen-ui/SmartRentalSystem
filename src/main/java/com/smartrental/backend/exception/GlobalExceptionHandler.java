package com.smartrental.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi Logic (VD: NullPointer, Sai pass, User not found...)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        // --- IN LỖI RA CONSOLE (QUAN TRỌNG ĐỂ DEBUG) ---
        System.err.println("❌ [Runtime Error] Backend bắt được lỗi: " + e.getMessage());

        // In toàn bộ Stack Trace để biết dòng code nào bị null
        e.printStackTrace();
        // --------------------------------------------------

        Map<String, String> response = new HashMap<>();
        response.put("error", "Lỗi hệ thống hoặc dữ liệu");
        response.put("message", e.getMessage()); // Trả message lỗi về cho Postman/Frontend

        // Trả về 400 Bad Request
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 2. Xử lý lỗi Validate (VD: @NotNull, @NotBlank, @Size...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        System.err.println("========================================");
        System.err.println("❌ [Validation Error] Dữ liệu gửi lên không hợp lệ:");

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();

            // In chi tiết từng lỗi ra Console
            System.err.println("   -> Trường '" + fieldName + "': " + errorMessage);

            errors.put(fieldName, errorMessage);
        });
        System.err.println("========================================");

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 3. Xử lý lỗi định dạng JSON (VD: Gửi chuỗi "abc" vào trường số "int")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonErrors(HttpMessageNotReadableException ex) {
        System.err.println("❌ [JSON PARSE ERROR] Frontend gửi dữ liệu Backend không đọc được!");
        System.err.println("👉 Chi tiết: " + ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", "Lỗi định dạng dữ liệu gửi lên (JSON Parse Error)");

        // Lấy thông tin ngắn gọn để trả về
        String msg = ex.getMessage();
        if (msg != null && msg.contains(":")) {
            msg = msg.split(":")[0]; // Cắt bớt phần dài dòng
        }
        response.put("details", msg);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}