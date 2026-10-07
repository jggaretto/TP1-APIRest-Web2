package com.desarrolloweb.tp1.dto;

import jakarta.validation.constraints.NotBlank;

public record ListaRequestDTO(

        @NotBlank(message = "nombre es obligatorio")
        String nombre
) {
}
