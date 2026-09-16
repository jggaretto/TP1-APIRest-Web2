package com.desarrolloweb.tp1.controller;

import com.desarrolloweb.tp1.dto.ProductoDTO;
import com.desarrolloweb.tp1.dto.ProductoPageDTO;
import com.desarrolloweb.tp1.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Catálogo de productos, de solo lectura, consumido desde DummyJSON")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Lista el catálogo de productos", description =
            "Devuelve productos mapeados a nuestro propio DTO. Soporta paginación vía limit/skip.")
    public ProductoPageDTO listar(
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "0") int skip) {
        return productoService.listarProductos(limit, skip);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id")
    public ProductoDTO obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerProducto(id);
    }
}
