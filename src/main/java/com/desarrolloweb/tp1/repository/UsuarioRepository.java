package com.desarrolloweb.tp1.repository;

import com.desarrolloweb.tp1.model.Usuario;

import java.util.Optional;

/**
 * Puerto de persistencia de Usuario, mismo patrón que FavoritoRepository y
 * ListaRepository: el dominio no conoce JPA ni Spring Data.
 */
public interface UsuarioRepository {

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
