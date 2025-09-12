package com.barbershop.features.barbershop.repository;

import com.barbershop.features.barbershop.model.BarbershopOperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar los horarios de operación de las barberías.
 * Proporciona métodos para consultar horarios por barbería, día de la semana,
 * y verificar disponibilidad.
 */
@Repository
public interface BarbershopOperatingHoursRepository extends JpaRepository<BarbershopOperatingHours, String> {

    /**
     * Busca todos los horarios de operación de una barbería específica
     * @param barbershopId ID de la barbería
     * @return Lista de horarios ordenados por día de la semana
     */
    @Query("SELECT boh FROM BarbershopOperatingHours boh " +
           "WHERE boh.barbershop.barbershopId = :barbershopId " +
           "ORDER BY boh.dayOfWeek")
    List<BarbershopOperatingHours> findByBarbershopIdOrderByDayOfWeek(@Param("barbershopId") String barbershopId);

    /**
     * Busca el horario de operación de una barbería para un día específico
     * @param barbershopId ID de la barbería
     * @param dayOfWeek Día de la semana
     * @return Horario de operación si existe
     */
    @Query("SELECT boh FROM BarbershopOperatingHours boh " +
           "WHERE boh.barbershop.barbershopId = :barbershopId " +
           "AND boh.dayOfWeek = :dayOfWeek")
    Optional<BarbershopOperatingHours> findByBarbershopIdAndDayOfWeek(
            @Param("barbershopId") String barbershopId, 
            @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Busca todas las barberías que están abiertas en un día específico
     * @param dayOfWeek Día de la semana
     * @return Lista de horarios de barberías abiertas
     */
    @Query("SELECT boh FROM BarbershopOperatingHours boh " +
           "WHERE boh.dayOfWeek = :dayOfWeek " +
           "AND boh.isClosed = false " +
           "AND boh.openingTime IS NOT NULL " +
           "AND boh.closingTime IS NOT NULL")
    List<BarbershopOperatingHours> findOpenBarbershopsByDayOfWeek(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Verifica si una barbería está abierta en un día específico
     * @param barbershopId ID de la barbería
     * @param dayOfWeek Día de la semana
     * @return true si está abierta
     */
    @Query("SELECT CASE WHEN COUNT(boh) > 0 THEN true ELSE false END " +
           "FROM BarbershopOperatingHours boh " +
           "WHERE boh.barbershop.barbershopId = :barbershopId " +
           "AND boh.dayOfWeek = :dayOfWeek " +
           "AND boh.isClosed = false " +
           "AND boh.openingTime IS NOT NULL " +
           "AND boh.closingTime IS NOT NULL")
    boolean isBarbershopOpenOnDay(
            @Param("barbershopId") String barbershopId, 
            @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * Elimina todos los horarios de una barbería específica
     * @param barbershopId ID de la barbería
     */
    @Query("DELETE FROM BarbershopOperatingHours boh " +
           "WHERE boh.barbershop.barbershopId = :barbershopId")
    void deleteByBarbershopId(@Param("barbershopId") String barbershopId);

    /**
     * Cuenta cuántos días tiene configurados una barbería
     * @param barbershopId ID de la barbería
     * @return Número de días configurados
     */
    @Query("SELECT COUNT(boh) FROM BarbershopOperatingHours boh " +
           "WHERE boh.barbershop.barbershopId = :barbershopId")
    long countByBarbershopId(@Param("barbershopId") String barbershopId);
}