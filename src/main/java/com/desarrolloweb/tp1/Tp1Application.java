package com.desarrolloweb.tp1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * La autenticación la resuelve nuestro AuthController + JwtAuthenticationFilter,
 * no un UserDetailsService. Excluimos esa auto-configuración para que Spring
 * Boot no genere un usuario en memoria con contraseña aleatoria.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class Tp1Application {

    public static void main(String[] args) {
        SpringApplication.run(Tp1Application.class, args);
    }
}
