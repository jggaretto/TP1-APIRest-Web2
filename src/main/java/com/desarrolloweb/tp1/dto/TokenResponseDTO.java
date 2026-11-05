package com.desarrolloweb.tp1.dto;

public record TokenResponseDTO(
        String token,
        String tipo,
        long expiraEn,
        UsuarioResponseDTO usuario
) {
}
