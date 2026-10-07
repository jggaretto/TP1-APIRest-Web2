package com.desarrolloweb.tp1.repository;

import com.desarrolloweb.tp1.model.Favorito;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository {

    Favorito save(Favorito favorito);

    List<Favorito> findAll();

    Optional<Favorito> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    List<Favorito> findByListaId(Long listaId);
}
