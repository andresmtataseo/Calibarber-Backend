package com.barbershop.features.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para la consulta de disponibilidad de barberos
 * en un horario específico, incluyendo el tiempo libre de cada uno
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarbersAvailabilityResponseDto {
    
    /**
     * Fecha y hora consultada en formato ISO
     */
    private LocalDateTime dateTime;
    
    /**
     * Lista de barberos con su disponibilidad y tiempo libre
     */
    private List<BarberAvailabilityDto> barbers;
}