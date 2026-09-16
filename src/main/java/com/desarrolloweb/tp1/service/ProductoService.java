package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.client.DummyJsonClient;
import com.desarrolloweb.tp1.client.DummyJsonModels.DummyJsonProduct;
import com.desarrolloweb.tp1.dto.ProductoDTO;
import com.desarrolloweb.tp1.dto.ProductoPageDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    private final DummyJsonClient dummyJsonClient;

    public ProductoService(DummyJsonClient dummyJsonClient) {
        this.dummyJsonClient = dummyJsonClient;
    }

    public ProductoPageDTO listarProductos(int limit, int skip) {
        var externo = dummyJsonClient.obtenerProductos(limit, skip);
        List<ProductoDTO> productos = externo.products().stream()
                .map(this::aProductoDTO)
                .toList();
        return new ProductoPageDTO(productos, externo.total(), externo.limit(), externo.skip());
    }

    public ProductoDTO obtenerProducto(Long id) {
        DummyJsonProduct externo = dummyJsonClient.obtenerProductoPorId(id);
        return aProductoDTO(externo);
    }

    private ProductoDTO aProductoDTO(DummyJsonProduct externo) {
        return new ProductoDTO(
                externo.id(),
                externo.title(),
                externo.description(),
                externo.category(),
                externo.price(),
                externo.rating(),
                externo.thumbnail()
        );
    }
}
