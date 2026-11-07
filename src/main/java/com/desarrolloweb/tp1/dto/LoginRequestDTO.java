package com.desarrolloweb.tp1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "email es obligatorio")
        @Email(message = "email debe tener un formato válido")
        String email,

        @NotBlank(message = "password es obligatorio")
        String password
) {
}
