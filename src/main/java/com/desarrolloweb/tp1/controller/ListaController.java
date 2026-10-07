package com.desarrolloweb.tp1.controller;

import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.dto.ListaRequestDTO;
import com.desarrolloweb.tp1.dto.ListaResponseDTO;
import com.desarrolloweb.tp1.dto.MoverFavoritosRequestDTO;
import com.desarrolloweb.tp1.service.ListaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listas")
@Tag(name = "Listas", description = "Listas para organizar favoritos, persistidas en PostgreSQL")
public class ListaController {

    private final ListaService listaService;

    public ListaController(ListaService listaService) {
        this.listaService = listaService;
    }

    @PostMapping
    @Operation(summary = "Crea una nueva lista")
    public ResponseEntity<ListaResponseDTO> crear(@Valid @RequestBody ListaRequestDTO request) {
        var creada = listaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping
    @Operation(summary = "Lista todas las listas")
    public List<ListaResponseDTO> listar() {
        return listaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una lista por id")
    public ListaResponseDTO obtener(@PathVariable Long id) {
        return listaService.obtener(id);
    }

    @GetMapping("/{id}/favoritos")
    @Operation(summary = "Lista los favoritos que pertenecen a una lista")
    public List<FavoritoResponseDTO> favoritosDe(@PathVariable Long id) {
        return listaService.favoritosDe(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una lista vacía (409 si todavía tiene favoritos)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        listaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{origenId}/mover-favoritos")
    @Operation(summary = "Mueve todos los favoritos de una lista a otra y elimina la lista origen",
            description = "Operación transaccional: si falla a mitad de camino, no queda nada aplicado.")
    public ResponseEntity<Void> moverFavoritos(@PathVariable Long origenId,
                                                @Valid @RequestBody MoverFavoritosRequestDTO request) {
        listaService.moverFavoritos(origenId, request.listaDestinoId());
        return ResponseEntity.noContent().build();
    }
}
