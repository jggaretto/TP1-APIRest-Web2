package com.desarrolloweb.tp1.repository.jpa;

import com.desarrolloweb.tp1.model.Favorito;
import com.desarrolloweb.tp1.repository.FavoritoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter de infraestructura: implementa el puerto FavoritoRepository (definido
 * en el TP1) delegando en Spring Data JPA. Convierte entre FavoritoEntity
 * (persistencia) y Favorito (dominio) en ambas direcciones.
 */
@Repository
@Profile("!memory")
public class FavoritoRepositoryAdapter implements FavoritoRepository {

    private final FavoritoJpaRepository favoritoJpaRepository;
    private final ListaJpaRepository listaJpaRepository;

    public FavoritoRepositoryAdapter(FavoritoJpaRepository favoritoJpaRepository,
                                      ListaJpaRepository listaJpaRepository) {
        this.favoritoJpaRepository = favoritoJpaRepository;
        this.listaJpaRepository = listaJpaRepository;
    }

    @Override
    public Favorito save(Favorito favorito) {
        ListaEntity lista = listaJpaRepository.getReferenceById(favorito.listaId());
        FavoritoEntity entity = new FavoritoEntity(
                favorito.id(),
                favorito.productoId(),
                favorito.nota(),
                favorito.fechaAlta(),
                lista);
        return aDominio(favoritoJpaRepository.save(entity));
    }

    @Override
    public List<Favorito> findAll() {
        return favoritoJpaRepository.findAll().stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return favoritoJpaRepository.findById(id).map(this::aDominio);
    }

    @Override
    public boolean existsById(Long id) {
        return favoritoJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        favoritoJpaRepository.deleteById(id);
    }

    @Override
    public List<Favorito> findByListaId(Long listaId) {
        return favoritoJpaRepository.findByListaId(listaId).stream()
                .map(this::aDominio)
                .toList();
    }

    private Favorito aDominio(FavoritoEntity entity) {
        return new Favorito(
                entity.getId(),
                entity.getProductoId(),
                entity.getLista().getId(),
                entity.getNota(),
                entity.getFechaAlta());
    }
}
