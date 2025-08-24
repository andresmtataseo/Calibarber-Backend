package com.barbershop.features.health.controller;

import com.barbershop.common.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar el estado de salud de la aplicación.
 * Proporciona endpoints para monitorear el estado del sistema.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Endpoints para verificar el estado de salud de la aplicación")
public class HealthController {

    /**
     * Endpoint para verificar el estado de salud de la aplicación.
     * 
     * @return ResponseEntity con el estado de salud del sistema
     */
    @GetMapping("/health")
    @Operation(
        summary = "Verificar estado de salud",
        description = "Retorna el estado actual de la aplicación y sus componentes"
    )
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("application", "Calibarber Backend");
        healthData.put("version", "1.0.0");
        healthData.put("environment", "development");
        
        // Información adicional del sistema
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
        systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        
        healthData.put("system", systemInfo);
        
        ApiResponseDto<Map<String, Object>> response = ApiResponseDto.<Map<String, Object>>builder()
                .status(200)
                .message("Aplicación funcionando correctamente")
                .data(healthData)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(response);
    }
}