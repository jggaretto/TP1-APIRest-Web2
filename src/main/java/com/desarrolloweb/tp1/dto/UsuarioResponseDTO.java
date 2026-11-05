package com.desarrolloweb.tp1.dto;

import com.desarrolloweb.tp1.model.Rol;
import com.desarrolloweb.tp1.model.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String email,
        Rol rol
) {
    public static UsuarioResponseDTO desde(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.id(), usuario.email(), usuario.rol());
    }
}
