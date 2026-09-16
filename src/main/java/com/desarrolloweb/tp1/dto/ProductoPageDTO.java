package com.desarrolloweb.tp1.dto;

import java.util.List;

/**
 * Contrato de paginación propio hacia el cliente, transformado a partir de
 * los parámetros limit/skip que usa DummyJSON.
 */
public record ProductoPageDTO(
        List<ProductoDTO> productos,
        int total,
        int limit,
        int skip
) {
}
