package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.FavoritoRequestDTO;
import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.exception.FavoritoNotFoundException;
import com.desarrolloweb.tp1.exception.ListaNotFoundException;
import com.desarrolloweb.tp1.model.Favorito;
import com.desarrolloweb.tp1.repository.FavoritoRepository;
import com.desarrolloweb.tp1.repository.ListaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ListaRepository listaRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, ListaRepository listaRepository) {
        this.favoritoRepository = favoritoRepository;
        this.listaRepository = listaRepository;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO request) {
        validarListaExiste(request.listaId());
        Favorito favorito = new Favorito(null, request.productoId(), request.listaId(),
                request.nota(), LocalDateTime.now());
        return aResponseDTO(favoritoRepository.save(favorito));
    }

    public List<FavoritoResponseDTO> listar() {
        return favoritoRepository.findAll().stream()
                .map(this::aResponseDTO)
                .toList();
    }

    public FavoritoResponseDTO obtener(Long id) {
        return favoritoRepository.findById(id)
                .map(this::aResponseDTO)
                .orElseThrow(() -> new FavoritoNotFoundException(id));
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO request) {
        Favorito existente = favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNotFoundException(id));
        validarListaExiste(request.listaId());
        Favorito actualizado = new Favorito(existente.id(), request.productoId(), request.listaId(),
                request.nota(), existente.fechaAlta());
        return aResponseDTO(favoritoRepository.save(actualizado));
    }

    public void eliminar(Long id) {
        if (!favoritoRepository.existsById(id)) {
            throw new FavoritoNotFoundException(id);
        }
        favoritoRepository.deleteById(id);
    }

    private void validarListaExiste(Long listaId) {
        if (!listaRepository.existsById(listaId)) {
            throw new ListaNotFoundException(listaId);
        }
    }

    private FavoritoResponseDTO aResponseDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.listaId(),
                favorito.nota(),
                favorito.fechaAlta());
    }
}
