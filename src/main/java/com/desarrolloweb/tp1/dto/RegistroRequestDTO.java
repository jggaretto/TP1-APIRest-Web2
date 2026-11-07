package com.desarrolloweb.tp1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequestDTO(

        @NotBlank(message = "email es obligatorio")
        @Email(message = "email debe tener un formato válido")
        @Size(max = 255, message = "el email no puede superar los 255 caracteres")
        String email,

        @NotBlank(message = "password es obligatorio")
        @Size(min = 8, max = 72,
                message = "la contraseña debe tener entre 8 y 72 caracteres (límite de BCrypt)")
        String password
) {
}
