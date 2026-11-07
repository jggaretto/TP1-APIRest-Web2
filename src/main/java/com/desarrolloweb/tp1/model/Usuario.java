package com.desarrolloweb.tp1.model;

/**
 * Usuario de dominio. La contraseña nunca se guarda en texto plano: el campo
 * passwordHash contiene el resultado de PasswordEncoder (BCrypt).
 */
public record Usuario(
        Long id,
        String email,
        String passwordHash,
        Rol rol
) {
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
