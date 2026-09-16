package com.desarrolloweb.tp1.dto;

/**
 * Representación propia de un producto, desacoplada del contrato JSON de DummyJSON.
 */
public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        String categoria,
        double precio,
        double calificacion,
        String imagenUrl
) {
}
