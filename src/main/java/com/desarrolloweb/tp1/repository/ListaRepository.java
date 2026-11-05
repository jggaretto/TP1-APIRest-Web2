package com.desarrolloweb.tp1.repository;

import com.desarrolloweb.tp1.model.Lista;

import java.util.List;
import java.util.Optional;

public interface ListaRepository {

    Lista save(Lista lista);

    List<Lista> findAll();

    List<Lista> findByUsuarioId(Long usuarioId);

    Optional<Lista> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);
}
