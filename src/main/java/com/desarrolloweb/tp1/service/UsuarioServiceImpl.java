package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.LoginRequestDTO;
import com.desarrolloweb.tp1.dto.RegistroRequestDTO;
import com.desarrolloweb.tp1.dto.UsuarioResponseDTO;
import com.desarrolloweb.tp1.exception.CredencialesInvalidasException;
import com.desarrolloweb.tp1.exception.EmailYaRegistradoException;
import com.desarrolloweb.tp1.model.Rol;
import com.desarrolloweb.tp1.model.Usuario;
import com.desarrolloweb.tp1.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioResponseDTO registrar(RegistroRequestDTO request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }

        // Nunca se guarda la contraseña original: BCrypt genera un hash con sal.
        String passwordHash = passwordEncoder.encode(request.password());
        Usuario usuario = new Usuario(null, email, passwordHash, Rol.USER);
        return UsuarioResponseDTO.desde(usuarioRepository.save(usuario));
    }

    @Override
    public Usuario autenticar(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(request.password(), usuario.passwordHash())) {
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
