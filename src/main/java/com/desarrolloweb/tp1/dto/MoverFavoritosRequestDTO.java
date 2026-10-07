package com.desarrolloweb.tp1.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MoverFavoritosRequestDTO(

        @NotNull(message = "listaDestinoId es obligatorio")
        @Positive(message = "listaDestinoId debe ser un número positivo")
        Long listaDestinoId
) {
}
