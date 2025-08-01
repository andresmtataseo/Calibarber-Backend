package com.barbershop.features.barbershop.model;

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
@Table(name = "barbershops")
@ToString(exclude = {"barbers", "services"})
@EqualsAndHashCode(exclude = {"barbers", "services"})
public class Barbershop implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "barbershop_id")
    private String barbershopId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address_text", nullable = false)
    private String addressText;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "operating_hours", columnDefinition = "JSON")
    private String operatingHours;

    @Column(name = "logo_url")
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relaciones
    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.barbershop.features.barber.model.Barber> barbers;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.barbershop.features.service.model.Service> services;
}