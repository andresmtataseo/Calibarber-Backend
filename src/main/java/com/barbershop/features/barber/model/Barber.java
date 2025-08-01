package com.barbershop.features.barber.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "barbers")
@ToString(exclude = {"barbershop", "barberAvailabilities", "appointments"})
@EqualsAndHashCode(exclude = {"barbershop", "barberAvailabilities", "appointments"})
public class Barber implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "barber_id")
    private String barberId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "barbershop_id", nullable = false)
    private String barbershopId;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private com.barbershop.features.user.model.User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", insertable = false, updatable = false)
    private com.barbershop.features.barbershop.model.Barbershop barbershop;

    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BarberAvailability> barberAvailabilities;

    @OneToMany(mappedBy = "barber", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.barbershop.features.appointment.model.Appointment> appointments;
}