package com.desarrolloweb.tp1.security;

import com.desarrolloweb.tp1.exception.UsuarioNoAutenticadoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Punto único para obtener la identidad autenticada. Los servicios de dominio
 * no dependen de Spring Security directamente: dependen de este componente.
 */
@Component
public class CurrentUser {

    public UsuarioAutenticado get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            throw new UsuarioNoAutenticadoException();
        }
        return usuario;
    }
}
