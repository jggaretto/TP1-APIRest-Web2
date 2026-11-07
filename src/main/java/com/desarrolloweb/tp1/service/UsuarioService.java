package com.desarrolloweb.tp1.service;

import com.desarrolloweb.tp1.dto.LoginRequestDTO;
import com.desarrolloweb.tp1.dto.RegistroRequestDTO;
import com.desarrolloweb.tp1.dto.UsuarioResponseDTO;
import com.desarrolloweb.tp1.model.Usuario;

public interface UsuarioService {

    UsuarioResponseDTO registrar(RegistroRequestDTO request);

    Usuario autenticar(LoginRequestDTO request);
}
