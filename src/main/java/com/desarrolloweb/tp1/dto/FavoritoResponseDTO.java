package com.desarrolloweb.tp1.dto;

import java.time.LocalDateTime;

public record FavoritoResponseDTO(
        Long id,
        Long productoId,
        Long listaId,
        String nota,
        LocalDateTime fechaAlta
) {
}
