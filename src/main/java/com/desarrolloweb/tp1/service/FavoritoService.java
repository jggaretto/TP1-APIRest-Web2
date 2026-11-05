package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.FavoritoRequestDTO;
import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.exception.AccesoDenegadoException;
import com.desarrolloweb.tp1.exception.FavoritoNotFoundException;
import com.desarrolloweb.tp1.exception.ListaNotFoundException;
import com.desarrolloweb.tp1.model.Favorito;
import com.desarrolloweb.tp1.model.Lista;
import com.desarrolloweb.tp1.repository.FavoritoRepository;
import com.desarrolloweb.tp1.repository.ListaRepository;
import com.desarrolloweb.tp1.security.CurrentUser;
import com.desarrolloweb.tp1.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ListaRepository listaRepository;
    private final CurrentUser currentUser;

    public FavoritoService(FavoritoRepository favoritoRepository,
                           ListaRepository listaRepository,
                           CurrentUser currentUser) {
        this.favoritoRepository = favoritoRepository;
        this.listaRepository = listaRepository;
        this.currentUser = currentUser;
    }

    public FavoritoResponseDTO crear(FavoritoRequestDTO request) {
        buscarListaConAcceso(request.listaId());
        Favorito favorito = new Favorito(null, request.productoId(), request.listaId(),
                request.nota(), LocalDateTime.now());
        return aResponseDTO(favoritoRepository.save(favorito));
    }

    public List<FavoritoResponseDTO> listar() {
        UsuarioAutenticado actual = currentUser.get();
        List<Favorito> favoritos = actual.esAdmin()
                ? favoritoRepository.findAll()
                : favoritoRepository.findByUsuarioId(actual.id());
        return favoritos.stream()
                .map(this::aResponseDTO)
                .toList();
    }

    public FavoritoResponseDTO obtener(Long id) {
        Favorito favorito = buscarFavorito(id);
        buscarListaConAcceso(favorito.listaId());
        return aResponseDTO(favorito);
    }

    public FavoritoResponseDTO actualizar(Long id, FavoritoRequestDTO request) {
        Favorito existente = buscarFavorito(id);
        buscarListaConAcceso(existente.listaId());
        buscarListaConAcceso(request.listaId());

        Favorito actualizado = new Favorito(existente.id(), request.productoId(), request.listaId(),
                request.nota(), existente.fechaAlta());
        return aResponseDTO(favoritoRepository.save(actualizado));
    }

    public void eliminar(Long id) {
        Favorito favorito = buscarFavorito(id);
        buscarListaConAcceso(favorito.listaId());
        favoritoRepository.deleteById(id);
    }

    private Favorito buscarFavorito(Long id) {
        return favoritoRepository.findById(id)
                .orElseThrow(() -> new FavoritoNotFoundException(id));
    }

    /**
     * Un favorito hereda el dueño de su lista: para operar sobre él hay que
     * poder operar sobre la lista que lo contiene (dueño o ADMIN).
     */
    private Lista buscarListaConAcceso(Long listaId) {
        Lista lista = listaRepository.findById(listaId)
                .orElseThrow(() -> new ListaNotFoundException(listaId));

        UsuarioAutenticado actual = currentUser.get();
        if (!actual.esAdmin() && !actual.esPropietarioDe(lista.usuarioId())) {
            throw new AccesoDenegadoException("La lista " + listaId + " no pertenece al usuario autenticado");
        }
        return lista;
    }

    private FavoritoResponseDTO aResponseDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.listaId(),
                favorito.nota(),
                favorito.fechaAlta());
    }
}
