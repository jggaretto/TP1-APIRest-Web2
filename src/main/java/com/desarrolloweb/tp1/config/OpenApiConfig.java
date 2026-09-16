package com.desarrolloweb.tp1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("TP1 - Catálogo y Favoritos")
                        .description("API REST en capas: catálogo de productos (consumo de DummyJSON) "
                                + "y CRUD de favoritos en memoria.")
                        .version("v1"));
    }
}
