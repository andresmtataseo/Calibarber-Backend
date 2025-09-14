package com.barbershop.features.barbershop.repository;

import com.barbershop.features.barbershop.model.BarbershopOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarbershopOperatingHoursRepository extends JpaRepository<BarbershopOperatingHours, String> {

    /**
     * Busca los horarios de operación por barbería y día de la semana.
     */
    Optional<BarbershopOperatingHours> findByBarbershop_BarbershopIdAndDayOfWeek(
        String barbershopId, DayOfWeek dayOfWeek);

    /**
     * Obtiene todos los horarios de operación de una barbería.
     */
    @Query("SELECT boh FROM BarbershopOperatingHours boh WHERE boh.barbershop.barbershopId = :barbershopId ORDER BY boh.dayOfWeek")
    List<BarbershopOperatingHours> findByBarbershopIdOrderByDayOfWeek(@Param("barbershopId") String barbershopId);

    /**
     * Verifica si existe un horario para una barbería y día específico.
     */
    boolean existsByBarbershop_BarbershopIdAndDayOfWeek(String barbershopId, DayOfWeek dayOfWeek);

    /**
     * Elimina todos los horarios de una barbería específica.
     */
    void deleteByBarbershop_BarbershopId(String barbershopId);
}