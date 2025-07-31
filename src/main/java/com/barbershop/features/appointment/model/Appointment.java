package com.barbershop.features.appointment.model;

import com.barbershop.features.appointment.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "appointments")
@ToString(exclude = {"client", "barber", "service", "payments"})
@EqualsAndHashCode(exclude = {"client", "barber", "service", "payments"})
public class Appointment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id")
    private String appointmentId;

    @Column(name = "barbershop_id", nullable = false)
    private String barbershopId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "barber_id", nullable = false)
    private String barberId;

    @Column(name = "service_id", nullable = false)
    private String serviceId;

    @Column(name = "appointment_datetime_start", nullable = false)
    private LocalDateTime appointmentDatetimeStart;

    @Column(name = "appointment_datetime_end", nullable = false)
    private LocalDateTime appointmentDatetimeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "notes")
    private String notes;

    @Column(name = "price_at_booking", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtBooking;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private com.barbershop.features.user.model.User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", insertable = false, updatable = false)
    private com.barbershop.features.barber.model.Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private com.barbershop.features.service.model.Service service;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.barbershop.features.payment.model.Payment> payments;
}