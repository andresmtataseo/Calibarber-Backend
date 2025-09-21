package com.barbershop.features.appointment.model.enums;

/**
 * Enum que representa los diferentes estados de disponibilidad de la barbería por día
 */
public enum AvailabilityStatus {
    
    /**
     * Día completamente libre - hay disponibilidad amplia sin citas o con suficientes huecos
     * que cubran el horario laboral
     */
    LIBRE,
    
    /**
     * Día parcialmente disponible - existen huecos libres pero no todos los intervalos 
     * están disponibles
     */
    PARCIALMENTE_DISPONIBLE,
    
    /**
     * Sin disponibilidad - todos los barberos están ocupados durante toda la jornada
     */
    SIN_DISPONIBILIDAD
}