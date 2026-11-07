package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.ListaRequestDTO;
import com.desarrolloweb.tp1.exception.AccesoDenegadoException;
import com.desarrolloweb.tp1.model.Lista;
import com.desarrolloweb.tp1.model.Rol;
import com.desarrolloweb.tp1.repository.FavoritoRepository;
import com.desarrolloweb.tp1.repository.ListaRepository;
import com.desarrolloweb.tp1.security.CurrentUser;
import com.desarrolloweb.tp1.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListaServiceImplTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ListaServiceImpl listaService;

    @Test
    void crear_usuarioAutenticado_asociaLaListaAlUsuarioDelToken() {
        UsuarioAutenticado usuario = new UsuarioAutenticado(7L, "ana@example.com", Rol.USER);
        when(currentUser.get()).thenReturn(usuario);
        when(listaRepository.save(any(Lista.class))).thenAnswer(invocacion -> {
            Lista guardada = invocacion.getArgument(0);
            return new Lista(1L, guardada.nombre(), guardada.usuarioId());
        });

        var respuesta = listaService.crear(new ListaRequestDTO("Regalos"));

        ArgumentCaptor<Lista> captor = ArgumentCaptor.forClass(Lista.class);
        verify(listaRepository).save(captor.capture());
        assertEquals(7L, captor.getValue().usuarioId(),
                "el dueño debe salir del usuario autenticado, no del body");
        assertEquals(7L, respuesta.usuarioId());
        assertEquals("Regalos", respuesta.nombre());
    }

    @Test
    void obtener_listaDeOtroUsuario_lanzaAccesoDenegado() {
        when(currentUser.get()).thenReturn(new UsuarioAutenticado(7L, "ana@example.com", Rol.USER));
        when(listaRepository.findById(99L))
                .thenReturn(Optional.of(new Lista(99L, "Ajena", 8L)));

        assertThrows(AccesoDenegadoException.class, () -> listaService.obtener(99L));
    }

    @Test
    void obtener_listaDeOtroUsuarioComoAdmin_devuelveLaLista() {
        when(currentUser.get()).thenReturn(new UsuarioAutenticado(1L, "admin@example.com", Rol.ADMIN));
        when(listaRepository.findById(99L))
                .thenReturn(Optional.of(new Lista(99L, "Ajena", 8L)));

        var respuesta = listaService.obtener(99L);

        assertEquals(99L, respuesta.id());
        assertEquals(8L, respuesta.usuarioId());
    }

    @Test
    void listar_usuarioComun_soloConsultaSusPropiasListas() {
        when(currentUser.get()).thenReturn(new UsuarioAutenticado(7L, "ana@example.com", Rol.USER));
        when(listaRepository.findByUsuarioId(7L))
                .thenReturn(List.of(new Lista(1L, "Regalos", 7L)));

        var respuesta = listaService.listar();

        assertEquals(1, respuesta.size());
        verify(listaRepository).findByUsuarioId(7L);
        verify(listaRepository, never()).findAll();
    }
}
