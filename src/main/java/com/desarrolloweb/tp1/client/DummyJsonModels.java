package com.desarrolloweb.tp1.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Representaciones internas del contrato JSON externo de DummyJSON.
 * No se exponen directamente a los clientes de nuestra API: se mapean a ProductoDTO.
 */
public class DummyJsonModels {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DummyJsonProduct(
            Long id,
            String title,
            String description,
            String category,
            double price,
            double rating,
            String thumbnail
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DummyJsonProductList(
            List<DummyJsonProduct> products,
            int total,
            int skip,
            int limit
    ) {
    }
}
