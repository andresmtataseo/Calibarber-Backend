package com.barbershop.features.appointment.repository;

import com.barbershop.features.appointment.model.Appointment;
import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // Consultas básicas por cliente
    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByClientId(@Param("clientId") String clientId);

    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findByClientId(@Param("clientId") String clientId, Pageable pageable);

    // Consultas básicas por barbero
    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByBarberId(@Param("barberId") String barberId);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findByBarberId(@Param("barberId") String barberId, Pageable pageable);

    // Consultas básicas por barbería
    @Query("SELECT a FROM Appointment a WHERE a.barbershopId = :barbershopId ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByBarbershopId(@Param("barbershopId") String barbershopId);

    @Query("SELECT a FROM Appointment a WHERE a.barbershopId = :barbershopId ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findByBarbershopId(@Param("barbershopId") String barbershopId, Pageable pageable);

    // Consultas por estado
    @Query("SELECT a FROM Appointment a WHERE a.status = :status ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByStatus(@Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findByStatus(@Param("status") AppointmentStatus status, Pageable pageable);

    // Consultas por cliente y estado
    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.status = :status ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByClientIdAndStatus(@Param("clientId") String clientId, @Param("status") AppointmentStatus status);

    // Método para contar citas activas por barbero
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.barberId = :barberId AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveAppointmentsByBarberId(@Param("barberId") String barberId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.serviceId = :serviceId AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveAppointmentsByServiceId(@Param("serviceId") String serviceId);

    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.status = :status ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findByClientIdAndStatus(@Param("clientId") String clientId, @Param("status") AppointmentStatus status, Pageable pageable);

    // Consultas por barbero y estado
    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.status = :status ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByBarberIdAndStatus(@Param("barberId") String barberId, @Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.status = :status ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findByBarberIdAndStatus(@Param("barberId") String barberId, @Param("status") AppointmentStatus status, Pageable pageable);

    // Consultas por rango de fechas
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDatetimeStart BETWEEN :startDate AND :endDate ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDatetimeStart BETWEEN :startDate AND :endDate ORDER BY a.appointmentDatetimeStart ASC")
    Page<Appointment> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Consultas por barbero y rango de fechas
    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.appointmentDatetimeStart BETWEEN :startDate AND :endDate ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findByBarberIdAndDateRange(@Param("barberId") String barberId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.appointmentDatetimeStart BETWEEN :startDate AND :endDate ORDER BY a.appointmentDatetimeStart ASC")
    Page<Appointment> findByBarberIdAndDateRange(@Param("barberId") String barberId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Consultas por cliente y rango de fechas
    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.appointmentDatetimeStart BETWEEN :startDate AND :endDate ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findByClientIdAndDateRange(@Param("clientId") String clientId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.appointmentDatetimeStart BETWEEN :startDate AND :endDate ORDER BY a.appointmentDatetimeStart ASC")
    Page<Appointment> findByClientIdAndDateRange(@Param("clientId") String clientId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Consultas para validar conflictos de horarios
    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') AND ((a.appointmentDatetimeStart < :endTime AND a.appointmentDatetimeEnd > :startTime))")
    List<Appointment> findConflictingAppointments(@Param("barberId") String barberId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.appointmentId != :appointmentId AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') AND ((a.appointmentDatetimeStart < :endTime AND a.appointmentDatetimeEnd > :startTime))")
    List<Appointment> findConflictingAppointmentsExcluding(@Param("barberId") String barberId, @Param("appointmentId") String appointmentId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // Consultas para citas del día
    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND DATE(a.appointmentDatetimeStart) = DATE(:date) ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findByBarberIdAndDate(@Param("barberId") String barberId, @Param("date") LocalDateTime date);

    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND DATE(a.appointmentDatetimeStart) = DATE(:date) ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findByClientIdAndDate(@Param("clientId") String clientId, @Param("date") LocalDateTime date);

    // Consultas con joins para información completa
    @Query("SELECT a FROM Appointment a JOIN FETCH a.client JOIN FETCH a.barber JOIN FETCH a.service WHERE a.appointmentId = :appointmentId")
    Optional<Appointment> findByIdWithDetails(@Param("appointmentId") String appointmentId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.client JOIN FETCH a.barber JOIN FETCH a.service WHERE a.clientId = :clientId ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByClientIdWithDetails(@Param("clientId") String clientId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.client JOIN FETCH a.barber JOIN FETCH a.service WHERE a.barberId = :barberId ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findByBarberIdWithDetails(@Param("barberId") String barberId);

    // Consultas para estadísticas
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.barberId = :barberId AND a.status = :status")
    long countByBarberIdAndStatus(@Param("barberId") String barberId, @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clientId = :clientId AND a.status = :status")
    long countByClientIdAndStatus(@Param("clientId") String clientId, @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.clientId = :clientId AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveAppointmentsByClientId(@Param("clientId") String clientId);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.barbershopId = :barbershopId AND a.status = :status")
    long countByBarbershopIdAndStatus(@Param("barbershopId") String barbershopId, @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.barberId = :barberId AND DATE(a.appointmentDatetimeStart) = DATE(:date)")
    long countByBarberIdAndDate(@Param("barberId") String barberId, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDatetimeStart >= :startOfDay AND a.appointmentDatetimeStart < :endOfDay")
    long countTodayAppointments(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // Consultas para próximas citas
    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.appointmentDatetimeStart > :now AND a.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findUpcomingByClientId(@Param("clientId") String clientId, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.appointmentDatetimeStart > :now AND a.status IN ('SCHEDULED', 'CONFIRMED') ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findUpcomingByBarberId(@Param("barberId") String barberId, @Param("now") LocalDateTime now);

    // Consultas para historial de citas
    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.status IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findHistoryByClientId(@Param("clientId") String clientId);

    @Query("SELECT a FROM Appointment a WHERE a.clientId = :clientId AND a.status IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findHistoryByClientId(@Param("clientId") String clientId, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.status IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY a.appointmentDatetimeStart DESC")
    List<Appointment> findHistoryByBarberId(@Param("barberId") String barberId);

    @Query("SELECT a FROM Appointment a WHERE a.barberId = :barberId AND a.status IN ('COMPLETED', 'CANCELLED', 'NO_SHOW') ORDER BY a.appointmentDatetimeStart DESC")
    Page<Appointment> findHistoryByBarberId(@Param("barberId") String barberId, Pageable pageable);

    // Consulta para obtener citas de múltiples barberos en un rango de fechas
    @Query("SELECT a FROM Appointment a WHERE a.barberId IN :barberIds AND a.appointmentDatetimeStart BETWEEN :startDate AND :endDate AND a.status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS') ORDER BY a.appointmentDatetimeStart ASC")
    List<Appointment> findByBarberIdInAndAppointmentDatetimeStartBetween(@Param("barberIds") List<String> barberIds, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Consulta para encontrar citas perdidas (para el servicio programado)
    @Query("SELECT a FROM Appointment a WHERE a.status IN ('SCHEDULED', 'CONFIRMED') AND a.appointmentDatetimeEnd < :currentTime ORDER BY a.appointmentDatetimeEnd ASC")
    List<Appointment> findMissedAppointments(@Param("currentTime") LocalDateTime currentTime);

    // Consulta para contar citas por estado en un rango de fechas (para estadísticas)
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status AND a.appointmentDatetimeStart BETWEEN :startDate AND :endDate")
    long countByStatusAndDateRange(@Param("status") AppointmentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}