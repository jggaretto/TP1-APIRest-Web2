package com.desarrolloweb.tp1.exception;

/**
 * La identidad autenticada existe, pero no puede operar sobre el recurso
 * pedido (por ejemplo, una lista que pertenece a otro usuario).
 * Se traduce a 403 Forbidden.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String message) {
        super(message);
    }
}
