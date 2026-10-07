package com.desarrolloweb.tp1.exception;

public class ListaNotFoundException extends RuntimeException {

    public ListaNotFoundException(Long id) {
        super("No existe una lista con id " + id);
    }
}
