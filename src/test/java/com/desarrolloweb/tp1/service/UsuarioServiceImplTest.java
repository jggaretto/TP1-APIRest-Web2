package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.LoginRequestDTO;
import com.desarrolloweb.tp1.dto.RegistroRequestDTO;
import com.desarrolloweb.tp1.exception.CredencialesInvalidasException;
import com.desarrolloweb.tp1.exception.EmailYaRegistradoException;
import com.desarrolloweb.tp1.model.Rol;
import com.desarrolloweb.tp1.model.Usuario;
import com.desarrolloweb.tp1.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String HASH_BCRYPT = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void registrar_nuncaGuardaLaPasswordEnTextoPlano() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn(HASH_BCRYPT);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario guardado = invocacion.getArgument(0);
            return new Usuario(1L, guardado.email(), guardado.passwordHash(), guardado.rol());
        });

        var respuesta = usuarioService.registrar(new RegistroRequestDTO("  Ana@Example.com ", "secreto123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(HASH_BCRYPT, captor.getValue().passwordHash());
        assertNotEquals("secreto123", captor.getValue().passwordHash(),
                "la contraseña original nunca debe persistirse");
        assertEquals(Rol.USER, captor.getValue().rol());
        assertEquals("ana@example.com", captor.getValue().email());
        assertEquals("ana@example.com", respuesta.email());
    }

    @Test
    void registrar_emailExistente_lanzaEmailYaRegistrado() {
        when(usuarioRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class,
                () -> usuarioService.registrar(new RegistroRequestDTO("ana@example.com", "secreto123")));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void autenticar_passwordIncorrecta_lanzaCredencialesInvalidas() {
        Usuario usuario = new Usuario(1L, "ana@example.com", HASH_BCRYPT, Rol.USER);
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", HASH_BCRYPT)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> usuarioService.autenticar(new LoginRequestDTO("ana@example.com", "incorrecta")));
    }

    @Test
    void autenticar_passwordCorrecta_devuelveElUsuario() {
        Usuario usuario = new Usuario(1L, "ana@example.com", HASH_BCRYPT, Rol.USER);
        when(usuarioRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreto123", HASH_BCRYPT)).thenReturn(true);

        var autenticado = usuarioService.autenticar(new LoginRequestDTO("ana@example.com", "secreto123"));

        assertEquals(1L, autenticado.id());
        assertEquals("ana@example.com", autenticado.email());
    }
}
