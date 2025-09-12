package com.barbershop.features.barbershop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad que representa los horarios de operación de una barbería por día de la semana.
 * Permite definir horarios de apertura y cierre específicos para cada día,
 * así como marcar días como cerrados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "barbershop_operating_hours", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"barbershop_id", "day_of_week"}))
public class BarbershopOperatingHours implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "operating_hours_id")
    private String operatingHoursId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    /**
     * Día de la semana (MONDAY, TUESDAY, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    /**
     * Hora de apertura del establecimiento
     */
    @Column(name = "opening_time")
    private LocalTime openingTime;

    /**
     * Hora de cierre del establecimiento
     */
    @Column(name = "closing_time")
    private LocalTime closingTime;

    /**
     * Indica si la barbería está cerrada este día
     * Si es true, openingTime y closingTime pueden ser null
     */
    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private Boolean isClosed = false;

    /**
     * Notas adicionales para el día (ej: "Horario especial por feriado")
     */
    @Column(name = "notes", length = 255)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Verifica si la barbería está abierta en este día
     * @return true si está abierta (no cerrada y tiene horarios definidos)
     */
    public boolean isOpen() {
        return !isClosed && openingTime != null && closingTime != null;
    }

    /**
     * Verifica si una hora específica está dentro del horario de operación
     * @param time Hora a verificar
     * @return true si está dentro del horario de operación
     */
    public boolean isWithinOperatingHours(LocalTime time) {
        if (!isOpen() || time == null) {
            return false;
        }
        return !time.isBefore(openingTime) && !time.isAfter(closingTime);
    }

    /**
     * Obtiene una representación legible del horario
     * @return String con el formato "HH:mm - HH:mm" o "Cerrado"
     */
    public String getFormattedHours() {
        if (isClosed) {
            return "Cerrado";
        }
        if (openingTime == null || closingTime == null) {
            return "Horario no definido";
        }
        return String.format("%s - %s", openingTime.toString(), closingTime.toString());
    }
}