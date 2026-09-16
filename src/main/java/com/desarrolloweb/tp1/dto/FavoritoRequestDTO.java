package com.desarrolloweb.tp1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FavoritoRequestDTO(

        @NotNull(message = "productoId es obligatorio")
        @Positive(message = "productoId debe ser un número positivo")
        Long productoId,

        @Size(max = 255, message = "la nota no puede superar los 255 caracteres")
        String nota
) {
}
