package com.desarrolloweb.tp1.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaJpaRepository extends JpaRepository<ListaEntity, Long> {

    List<ListaEntity> findByUsuario_Id(Long usuarioId);
}
