package com.desarrolloweb.tp1.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio de Favorito. Es un record inmutable: no se toca al migrar
 * de persistencia en memoria (TP1) a JPA (TP2) — el puerto FavoritoRepository
 * sigue devolviendo/recibiendo este mismo tipo sin importar el adapter detrás.
 */
public record Favorito(
        Long id,
        Long productoId,
        Long listaId,
        String nota,
        LocalDateTime fechaAlta
) {
}
