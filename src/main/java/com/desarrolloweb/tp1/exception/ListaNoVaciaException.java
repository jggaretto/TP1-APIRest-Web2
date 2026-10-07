package com.desarrolloweb.tp1.exception;

public class ListaNoVaciaException extends RuntimeException {

    public ListaNoVaciaException(Long id) {
        super("No se puede eliminar la lista " + id + " porque todavía tiene favoritos");
    }
}
