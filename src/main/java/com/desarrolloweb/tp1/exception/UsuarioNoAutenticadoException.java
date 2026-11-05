package com.desarrolloweb.tp1.exception;

/**
 * Se lanza cuando un endpoint protegido se ejecuta sin identidad autenticada.
 * En la práctica Spring Security corta antes (401); queda como red de
 * seguridad para llamadas internas o tests.
 */
public class UsuarioNoAutenticadoException extends RuntimeException {

    public UsuarioNoAutenticadoException() {
        super("No hay un usuario autenticado en el contexto de seguridad");
    }
}
