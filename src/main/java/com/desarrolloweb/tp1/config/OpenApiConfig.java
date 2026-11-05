package com.desarrolloweb.tp1.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("TP3 - Catálogo, Favoritos y Listas con seguridad")
                        .description("API REST en capas: catálogo de productos (consumo de DummyJSON), "
                                + "CRUD de favoritos y listas persistidos en PostgreSQL, "
                                + "autenticación con JWT y autorización por rol/propiedad. "
                                + "Los endpoints de /api/favoritos y /api/listas requieren autenticación.")
                        .version("v3"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pegá el token devuelto por POST /api/auth/login")));
    }
}
