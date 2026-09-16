package com.desarrolloweb.tp1.exception;

public class FavoritoNotFoundException extends RuntimeException {

    public FavoritoNotFoundException(Long id) {
        super("No existe un favorito con id " + id);
    }
}
