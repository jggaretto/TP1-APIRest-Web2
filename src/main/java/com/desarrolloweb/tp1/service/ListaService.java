package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.dto.ListaRequestDTO;
import com.desarrolloweb.tp1.dto.ListaResponseDTO;
import com.desarrolloweb.tp1.exception.ListaNoVaciaException;
import com.desarrolloweb.tp1.exception.ListaNotFoundException;
import com.desarrolloweb.tp1.model.Favorito;
import com.desarrolloweb.tp1.model.Lista;
import com.desarrolloweb.tp1.repository.FavoritoRepository;
import com.desarrolloweb.tp1.repository.ListaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;

    public ListaService(ListaRepository listaRepository, FavoritoRepository favoritoRepository) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
    }

    public ListaResponseDTO crear(ListaRequestDTO request) {
        Lista lista = new Lista(null, request.nombre());
        return aResponseDTO(listaRepository.save(lista));
    }

    public List<ListaResponseDTO> listar() {
        return listaRepository.findAll().stream()
                .map(this::aResponseDTO)
                .toList();
    }

    public ListaResponseDTO obtener(Long id) {
        return listaRepository.findById(id)
                .map(this::aResponseDTO)
                .orElseThrow(() -> new ListaNotFoundException(id));
    }

    public List<FavoritoResponseDTO> favoritosDe(Long listaId) {
        if (!listaRepository.existsById(listaId)) {
            throw new ListaNotFoundException(listaId);
        }
        return favoritoRepository.findByListaId(listaId).stream()
                .map(this::aFavoritoResponseDTO)
                .toList();
    }

    public void eliminar(Long id) {
        if (!listaRepository.existsById(id)) {
            throw new ListaNotFoundException(id);
        }
        if (!favoritoRepository.findByListaId(id).isEmpty()) {
            throw new ListaNoVaciaException(id);
        }
        listaRepository.deleteById(id);
    }

    /**
     * Operación transaccional explícita: valida que ambas listas existan,
     * reasigna todos los favoritos de la lista origen a la destino, y recién
     * entonces elimina la lista origen (ya vacía). Si cualquiera de las
     * escrituras falla, @Transactional revierte todo lo hecho hasta ese punto
     * — no puede quedar un favorito "reasignado" sin que la lista origen se
     * haya borrado, ni una lista origen borrada con favoritos que quedaron
     * apuntando a una lista inexistente.
     */
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        if (!listaRepository.existsById(origenId)) {
            throw new ListaNotFoundException(origenId);
        }
        if (!listaRepository.existsById(destinoId)) {
            throw new ListaNotFoundException(destinoId);
        }

        List<Favorito> favoritosDeOrigen = favoritoRepository.findByListaId(origenId);
        for (Favorito favorito : favoritosDeOrigen) {
            Favorito reasignado = new Favorito(favorito.id(), favorito.productoId(), destinoId,
                    favorito.nota(), favorito.fechaAlta());
            favoritoRepository.save(reasignado);
        }

        listaRepository.deleteById(origenId);
    }

    private ListaResponseDTO aResponseDTO(Lista lista) {
        return new ListaResponseDTO(lista.id(), lista.nombre());
    }

    private FavoritoResponseDTO aFavoritoResponseDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.listaId(),
                favorito.nota(),
                favorito.fechaAlta());
    }
}
