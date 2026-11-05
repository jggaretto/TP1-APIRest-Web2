package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.dto.ListaRequestDTO;
import com.desarrolloweb.tp1.dto.ListaResponseDTO;

import java.util.List;

public interface ListaService {

    ListaResponseDTO crear(ListaRequestDTO request);

    List<ListaResponseDTO> listar();

    ListaResponseDTO obtener(Long id);

    List<FavoritoResponseDTO> favoritosDe(Long listaId);

    void eliminar(Long id);

    void moverFavoritos(Long origenId, Long destinoId);
}
