package com.desarrolloweb.tp1.repository.jpa;

import com.desarrolloweb.tp1.model.Lista;
import com.desarrolloweb.tp1.repository.ListaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ListaRepositoryAdapter implements ListaRepository {

    private final ListaJpaRepository listaJpaRepository;

    public ListaRepositoryAdapter(ListaJpaRepository listaJpaRepository) {
        this.listaJpaRepository = listaJpaRepository;
    }

    @Override
    public Lista save(Lista lista) {
        ListaEntity entity = new ListaEntity(lista.id(), lista.nombre());
        return aDominio(listaJpaRepository.save(entity));
    }

    @Override
    public List<Lista> findAll() {
        return listaJpaRepository.findAll().stream()
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
        return new Lista(entity.getId(), entity.getNombre());
    }
}
