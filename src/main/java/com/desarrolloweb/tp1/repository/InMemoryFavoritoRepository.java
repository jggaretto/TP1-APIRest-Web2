package com.desarrolloweb.tp1.repository;

import com.desarrolloweb.tp1.model.Favorito;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryFavoritoRepository implements FavoritoRepository {

    private final Map<Long, Favorito> favoritos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    @Override
    public Favorito save(Favorito favorito) {
        if (favorito.getId() == null) {
            favorito.setId(secuencia.incrementAndGet());
        }
        favoritos.put(favorito.getId(), favorito);
        return favorito;
    }

    @Override
    public List<Favorito> findAll() {
        return new ArrayList<>(favoritos.values());
    }

    @Override
    public Optional<Favorito> findById(Long id) {
        return Optional.ofNullable(favoritos.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return favoritos.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        favoritos.remove(id);
    }
}
