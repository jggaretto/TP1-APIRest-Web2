package com.desarrolloweb.tp1.controller;

import com.desarrolloweb.tp1.dto.FavoritoRequestDTO;
import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "CRUD de favoritos, persistidos en memoria")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping
    @Operation(summary = "Crea un nuevo favorito")
    public ResponseEntity<FavoritoResponseDTO> crear(@Valid @RequestBody FavoritoRequestDTO request) {
        var creado = favoritoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    @Operation(summary = "Lista todos los favoritos")
    public List<FavoritoResponseDTO> listar() {
        return favoritoService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un favorito por id")
    public FavoritoResponseDTO obtener(@PathVariable Long id) {
        return favoritoService.obtener(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un favorito existente")
    public FavoritoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody FavoritoRequestDTO request) {
        return favoritoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un favorito")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        favoritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
