package com.barbershop.features.user.controller;

import com.barbershop.common.dto.ApiResponseDto;
import com.barbershop.common.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.USER_API_BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios")
public class UserController {

    @Operation(
            summary = "Obtiene todos los usuarios",
            description = "Devuelve una lista con todos los usuarios registrados en el sistema.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Operación exitosa",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))
                    )
            }
    )
    @GetMapping(ApiConstants.USER_ALL_URL)
    public ResponseEntity<ApiResponseDto<String>> findAll() {
        // Implementación temporal - se debe reemplazar con la lógica real
        return ResponseEntity.ok(ApiResponseDto.success(
                "Usuarios obtenidos exitosamente",
                "Todos los usuarios"
        ));
    }

}
