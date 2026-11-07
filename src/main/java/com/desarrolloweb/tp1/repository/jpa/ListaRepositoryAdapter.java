package com.desarrolloweb.tp1.repository.jpa;

import com.desarrolloweb.tp1.model.Lista;
import com.desarrolloweb.tp1.repository.ListaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository listaJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository listaJpaRepository,
                                  UsuarioJpaRepository usuarioJpaRepository) {
        this.listaJpaRepository = listaJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Lista save(Lista lista) {
        UsuarioEntity usuario = usuarioJpaRepository.getReferenceById(lista.usuarioId());
        ListaEntity entity = new ListaEntity(lista.id(), lista.nombre(), usuario);
        return aDominio(listaJpaRepository.save(entity));
    }

    @Override
    public List<Lista> findAll() {
        return listaJpaRepository.findAll().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<Lista> findByUsuarioId(Long usuarioId) {
        return listaJpaRepository.findByUsuario_Id(usuarioId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Lista> findById(Long id) {
        return listaJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public boolean existsById(Long id) {
        return listaJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        listaJpaRepository.deleteById(id);
    }

    private Lista aDominio(ListaEntity entity) {
        return new Lista(entity.getId(), entity.getNombre(), entity.getUsuario().getId());
    }
}
