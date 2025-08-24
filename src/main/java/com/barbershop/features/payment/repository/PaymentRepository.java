package com.barbershop.features.payment.repository;

import com.barbershop.features.payment.model.Payment;
import com.barbershop.features.payment.model.enums.PaymentMethod;
import com.barbershop.features.payment.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    // ========== CONSULTAS BÁSICAS ==========

    /**
     * Busca un pago por ID con información de la cita asociada
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Optional<Payment> findByPaymentId(String paymentId);

    /**
     * Busca todos los pagos con información de citas asociadas (paginado)
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findAll(Pageable pageable);

    // ========== CONSULTAS POR CITA ==========

    /**
     * Busca pagos por ID de cita
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    List<Payment> findByAppointmentId(String appointmentId);

    /**
     * Busca pagos por ID de cita con paginación
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByAppointmentId(String appointmentId, Pageable pageable);

    /**
     * Verifica si existe un pago para una cita específica
     */
    boolean existsByAppointmentId(String appointmentId);

    // ========== CONSULTAS POR ESTADO ==========

    /**
     * Busca pagos por estado
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByPaymentStatus(PaymentStatus status, Pageable pageable);

    /**
     * Cuenta pagos por estado
     */
    long countByPaymentStatus(PaymentStatus status);

    // ========== CONSULTAS POR MÉTODO DE PAGO ==========

    /**
     * Busca pagos por método de pago
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByPaymentMethod(PaymentMethod method, Pageable pageable);

    /**
     * Cuenta pagos por método de pago
     */
    long countByPaymentMethod(PaymentMethod method);

    // ========== CONSULTAS POR RANGO DE FECHAS ==========

    /**
     * Busca pagos creados en un rango de fechas
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Busca pagos realizados en un rango de fechas
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ========== CONSULTAS POR MONTO ==========

    /**
     * Busca pagos por rango de monto
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * Busca pagos mayores a un monto específico
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByAmountGreaterThan(BigDecimal amount, Pageable pageable);

    // ========== CONSULTAS COMBINADAS ==========

    /**
     * Busca pagos por estado y método de pago
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByPaymentStatusAndPaymentMethod(PaymentStatus status, PaymentMethod method, Pageable pageable);

    /**
     * Busca pagos por estado en un rango de fechas
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByPaymentStatusAndCreatedAtBetween(PaymentStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    // ========== CONSULTAS ESTADÍSTICAS ==========

    /**
     * Suma total de pagos completados
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    /**
     * Suma total de pagos completados en un rango de fechas
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByStatusAndDateRange(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Suma total de pagos por método de pago
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentMethod = :method AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumAmountByPaymentMethod(@Param("method") PaymentMethod method);

    // ========== CONSULTAS POR CLIENTE ==========

    /**
     * Busca pagos de un cliente específico
     */
    @Query("SELECT p FROM Payment p JOIN p.appointment a WHERE a.clientId = :clientId")
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByClientId(@Param("clientId") String clientId, Pageable pageable);

    /**
     * Suma total de pagos de un cliente
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.appointment a WHERE a.clientId = :clientId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumAmountByClientId(@Param("clientId") String clientId);

    // ========== CONSULTAS POR BARBERO ==========

    /**
     * Busca pagos de servicios realizados por un barbero específico
     */
    @Query("SELECT p FROM Payment p JOIN p.appointment a WHERE a.barberId = :barberId")
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    Page<Payment> findByBarberId(@Param("barberId") String barberId, Pageable pageable);

    /**
     * Suma total de pagos de servicios de un barbero
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.appointment a WHERE a.barberId = :barberId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumAmountByBarberId(@Param("barberId") String barberId);

    // ========== CONSULTAS DE PAGOS PENDIENTES ==========

    /**
     * Busca pagos pendientes más antiguos que una fecha específica
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime date);

    /**
     * Busca pagos fallidos para reintento
     */
    @EntityGraph(attributePaths = {"appointment", "appointment.client", "appointment.barber", "appointment.service"})
    List<Payment> findByPaymentStatusAndUpdatedAtBefore(PaymentStatus status, LocalDateTime date);
}