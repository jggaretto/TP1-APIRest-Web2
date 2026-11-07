package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.FavoritoResponseDTO;
import com.desarrolloweb.tp1.dto.ListaRequestDTO;
import com.desarrolloweb.tp1.dto.ListaResponseDTO;
import com.desarrolloweb.tp1.exception.AccesoDenegadoException;
import com.desarrolloweb.tp1.exception.ListaNoVaciaException;
import com.desarrolloweb.tp1.exception.ListaNotFoundException;
import com.desarrolloweb.tp1.model.Favorito;
import com.desarrolloweb.tp1.model.Lista;
import com.desarrolloweb.tp1.repository.FavoritoRepository;
import com.desarrolloweb.tp1.repository.ListaRepository;
import com.desarrolloweb.tp1.security.CurrentUser;
import com.desarrolloweb.tp1.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * La autorización por propiedad vive acá, en la capa de servicio, y no en el
 * Controller: es una regla de negocio ("solo el dueño o un ADMIN") y así vale
 * para cualquier punto de entrada (HTTP, tests, futuros jobs), además de
 * mantener los controllers como simples traductores HTTP.
 */
@Service
public class ListaServiceImpl implements ListaService {

    private final ListaRepository listaRepository;
    private final FavoritoRepository favoritoRepository;
    private final CurrentUser currentUser;

    public ListaServiceImpl(ListaRepository listaRepository,
                            FavoritoRepository favoritoRepository,
                            CurrentUser currentUser) {
        this.listaRepository = listaRepository;
        this.favoritoRepository = favoritoRepository;
        this.currentUser = currentUser;
    }

    @Override
    public ListaResponseDTO crear(ListaRequestDTO request) {
        UsuarioAutenticado actual = currentUser.get();
        // El dueño sale de la identidad autenticada, nunca del body.
        Lista lista = new Lista(null, request.nombre(), actual.id());
        return aResponseDTO(listaRepository.save(lista));
    }

    @Override
    public List<ListaResponseDTO> listar() {
        UsuarioAutenticado actual = currentUser.get();
        List<Lista> listas = actual.esAdmin()
                ? listaRepository.findAll()
                : listaRepository.findByUsuarioId(actual.id());
        return listas.stream()
                .map(this::aResponseDTO)
                .toList();
    }

    @Override
    public ListaResponseDTO obtener(Long id) {
        return aResponseDTO(buscarConAcceso(id));
    }

    @Override
    public List<FavoritoResponseDTO> favoritosDe(Long listaId) {
        buscarConAcceso(listaId);
        return favoritoRepository.findByListaId(listaId).stream()
                .map(this::aFavoritoResponseDTO)
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        buscarConAcceso(id);
        if (!favoritoRepository.findByListaId(id).isEmpty()) {
            throw new ListaNoVaciaException(id);
        }
        listaRepository.deleteById(id);
    }

    /**
     * Operación transaccional explícita: valida que ambas listas existan y que
     * el usuario pueda operar sobre las dos, reasigna todos los favoritos de la
     * lista origen a la destino, y recién entonces elimina la lista origen.
     * Si cualquiera de las escrituras falla, @Transactional revierte todo.
     */
    @Override
    @Transactional
    public void moverFavoritos(Long origenId, Long destinoId) {
        buscarConAcceso(origenId);
        buscarConAcceso(destinoId);

        List<Favorito> favoritosDeOrigen = favoritoRepository.findByListaId(origenId);
        for (Favorito favorito : favoritosDeOrigen) {
            Favorito reasignado = new Favorito(favorito.id(), favorito.productoId(), destinoId,
                    favorito.nota(), favorito.fechaAlta());
            favoritoRepository.save(reasignado);
        }

        listaRepository.deleteById(origenId);
    }

    /**
     * Devuelve 404 si la lista no existe y 403 si existe pero no es del usuario
     * autenticado (salvo ADMIN). Es intencional no devolver 404 en el segundo
     * caso: "no existe" y "no es tuya" son cosas distintas.
     */
    private Lista buscarConAcceso(Long id) {
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new ListaNotFoundException(id));

        UsuarioAutenticado actual = currentUser.get();
        if (!actual.esAdmin() && !actual.esPropietarioDe(lista.usuarioId())) {
            throw new AccesoDenegadoException("La lista " + id + " no pertenece al usuario autenticado");
        }
        return lista;
    }

    private ListaResponseDTO aResponseDTO(Lista lista) {
        return new ListaResponseDTO(lista.id(), lista.nombre(), lista.usuarioId());
    }

    private FavoritoResponseDTO aFavoritoResponseDTO(Favorito favorito) {
        return new FavoritoResponseDTO(
                favorito.id(),
                favorito.productoId(),
                favorito.listaId(),
                favorito.nota(),
                favorito.fechaAlta());
    }
}
