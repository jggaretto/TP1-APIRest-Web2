package com.desarrolloweb.tp1.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, Long> {

    List<FavoritoEntity> findByListaId(Long listaId);

    boolean existsByListaId(Long listaId);

    List<FavoritoEntity> findByLista_Usuario_Id(Long usuarioId);
}
