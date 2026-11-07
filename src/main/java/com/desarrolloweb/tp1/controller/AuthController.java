package com.desarrolloweb.tp1.controller;

import com.desarrolloweb.tp1.dto.LoginRequestDTO;
import com.desarrolloweb.tp1.dto.RegistroRequestDTO;
import com.desarrolloweb.tp1.dto.TokenResponseDTO;
import com.desarrolloweb.tp1.dto.UsuarioResponseDTO;
import com.desarrolloweb.tp1.security.JwtService;
import com.desarrolloweb.tp1.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión. El login devuelve un JWT para el resto de la API.")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registra un usuario nuevo",
            description = "Guarda la contraseña hasheada con BCrypt y asigna el rol USER. Devuelve 409 si el email ya existe.")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un JWT",
            description = "Valida email y contraseña contra el hash guardado. Devuelve 401 si las credenciales son inválidas.")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        var usuario = usuarioService.autenticar(request);
        return new TokenResponseDTO(
                jwtService.generar(usuario),
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                UsuarioResponseDTO.desde(usuario));
    }
}
