package com.desarrolloweb.tp1.model;

/**
 * Lista de favoritos. A partir del TP3 pertenece a un Usuario: el dueño se
 * asigna desde la identidad autenticada, nunca desde el body del request.
 */
public record Lista(
        Long id,
        String nombre,
        Long usuarioId
) {
}
