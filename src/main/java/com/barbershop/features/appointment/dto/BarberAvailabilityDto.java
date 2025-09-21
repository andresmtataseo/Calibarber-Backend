package com.barbershop.features.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la disponibilidad de un barbero específico
 * en un horario determinado, incluyendo el tiempo libre hasta su próxima cita
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberAvailabilityDto {
    
    /**
     * ID único del barbero
     */
    private String id;
    
    /**
     * Nombre completo del barbero
     */
    private String name;
    
    /**
     * Indica si el barbero está disponible en el horario consultado
     * true = puede atender una nueva cita
     * false = está ocupado con otra cita
     */
    private boolean available;
    
    /**
     * Tiempo libre disponible en minutos hasta la próxima cita
     * Si no tiene más citas, será el tiempo hasta el cierre de la barbería
     * Si está ocupado, será 0
     */
    private int freeMinutes;
}