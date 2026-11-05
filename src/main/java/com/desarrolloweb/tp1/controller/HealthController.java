package com.desarrolloweb.tp1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de estado simple y público, pensado para el despliegue del TP3.
 */
@RestController
@Tag(name = "Estado", description = "Endpoint público para verificar que el servicio está vivo")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Estado del servicio")
    public Map<String, Object> health() {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("status", "UP");
        respuesta.put("timestamp", OffsetDateTime.now());
        return respuesta;
    }
}
