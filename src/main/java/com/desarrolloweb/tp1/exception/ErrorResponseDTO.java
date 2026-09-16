package com.desarrolloweb.tp1.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> detalles
) {
    public ErrorResponseDTO(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    public ErrorResponseDTO(int status, String error, String message, Map<String, String> detalles) {
        this(LocalDateTime.now(), status, error, message, detalles);
    }
}
