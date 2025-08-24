package com.barbershop.features.barber.repository;

import com.barbershop.features.barber.model.BarberAvailability;
import com.barbershop.features.barber.model.DayOfWeek;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BarberAvailabilityRepository extends JpaRepository<BarberAvailability, String> {

    // Consultas básicas por barbero
    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.isAvailable = true ORDER BY ba.dayOfWeek, ba.startTime")
    List<BarberAvailability> findByBarberIdAndAvailable(@Param("barberId") String barberId);

    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId ORDER BY ba.dayOfWeek, ba.startTime")
    List<BarberAvailability> findByBarberId(@Param("barberId") String barberId);

    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId")
    Page<BarberAvailability> findByBarberId(@Param("barberId") String barberId, Pageable pageable);

    // Consultas por día de la semana
    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek AND ba.isAvailable = true ORDER BY ba.startTime")
    List<BarberAvailability> findByBarberIdAndDayOfWeekAndAvailable(@Param("barberId") String barberId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.dayOfWeek = :dayOfWeek AND ba.isAvailable = true ORDER BY ba.barberId, ba.startTime")
    List<BarberAvailability> findByDayOfWeekAndAvailable(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    // Consultas por rango de tiempo
    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek AND ba.startTime <= :endTime AND ba.endTime >= :startTime AND ba.isAvailable = true")
    List<BarberAvailability> findByBarberIdAndDayOfWeekAndTimeRangeAndAvailable(
            @Param("barberId") String barberId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Validaciones de solapamiento de horarios
    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek AND ba.barberAvailabilityId != :excludeId AND ((ba.startTime <= :startTime AND ba.endTime > :startTime) OR (ba.startTime < :endTime AND ba.endTime >= :endTime) OR (ba.startTime >= :startTime AND ba.endTime <= :endTime))")
    List<BarberAvailability> findOverlappingAvailability(
            @Param("barberId") String barberId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") String excludeId
    );

    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek AND ((ba.startTime <= :startTime AND ba.endTime > :startTime) OR (ba.startTime < :endTime AND ba.endTime >= :endTime) OR (ba.startTime >= :startTime AND ba.endTime <= :endTime))")
    List<BarberAvailability> findOverlappingAvailabilityForNew(
            @Param("barberId") String barberId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // Consultas de existencia
    @Query("SELECT CASE WHEN COUNT(ba) > 0 THEN true ELSE false END FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek AND ba.isAvailable = true")
    boolean existsByBarberIdAndDayOfWeekAndAvailable(@Param("barberId") String barberId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT CASE WHEN COUNT(ba) > 0 THEN true ELSE false END FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.isAvailable = true")
    boolean existsByBarberIdAndAvailable(@Param("barberId") String barberId);

    // Conteos
    @Query("SELECT COUNT(ba) FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.isAvailable = true")
    long countByBarberIdAndAvailable(@Param("barberId") String barberId);

    @Query("SELECT COUNT(ba) FROM BarberAvailability ba WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek AND ba.isAvailable = true")
    long countByBarberIdAndDayOfWeekAndAvailable(@Param("barberId") String barberId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    // Operaciones de actualización masiva
    @Modifying
    @Query("UPDATE BarberAvailability ba SET ba.isAvailable = :isAvailable, ba.updatedAt = :updatedAt WHERE ba.barberId = :barberId")
    void updateAvailabilityByBarberId(@Param("barberId") String barberId, @Param("isAvailable") Boolean isAvailable, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE BarberAvailability ba SET ba.isAvailable = :isAvailable, ba.updatedAt = :updatedAt WHERE ba.barberId = :barberId AND ba.dayOfWeek = :dayOfWeek")
    void updateAvailabilityByBarberIdAndDayOfWeek(@Param("barberId") String barberId, @Param("dayOfWeek") DayOfWeek dayOfWeek, @Param("isAvailable") Boolean isAvailable, @Param("updatedAt") LocalDateTime updatedAt);

    // Eliminación por barbero (cuando se elimina un barbero)
    @Modifying
    @Query("DELETE FROM BarberAvailability ba WHERE ba.barberId = :barberId")
    void deleteByBarberId(@Param("barberId") String barberId);

    // Consultas para obtener disponibilidad con información del barbero
    @Query("SELECT ba FROM BarberAvailability ba JOIN FETCH ba.barber b JOIN FETCH b.user WHERE ba.dayOfWeek = :dayOfWeek AND ba.isAvailable = true ORDER BY ba.startTime")
    List<BarberAvailability> findByDayOfWeekAndAvailableWithBarberAndUser(@Param("dayOfWeek") DayOfWeek dayOfWeek);

    @Query("SELECT ba FROM BarberAvailability ba JOIN FETCH ba.barber b JOIN FETCH b.user WHERE ba.barberId = :barberId AND ba.isAvailable = true ORDER BY ba.dayOfWeek, ba.startTime")
    List<BarberAvailability> findByBarberIdAndAvailableWithBarberAndUser(@Param("barberId") String barberId);

    // Consultas específicas para búsqueda de slots disponibles
    @Query("SELECT ba FROM BarberAvailability ba WHERE ba.barber.barbershopId = :barbershopId AND ba.dayOfWeek = :dayOfWeek AND ba.startTime <= :time AND ba.endTime > :time AND ba.isAvailable = true")
    List<BarberAvailability> findAvailableBarbersAtTime(
            @Param("barbershopId") String barbershopId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("time") LocalTime time
    );
}