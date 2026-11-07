package com.desarrolloweb.tp1.security;

import com.desarrolloweb.tp1.model.Rol;

/**
 * Identidad que deja el filtro JWT en el SecurityContext. Es el principal
 * que consumen los servicios para saber quién hace la request.
 */
public record UsuarioAutenticado(
        Long id,
        String email,
        Rol rol
) {
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean esPropietarioDe(Long usuarioId) {
        return id.equals(usuarioId);
    }
}
