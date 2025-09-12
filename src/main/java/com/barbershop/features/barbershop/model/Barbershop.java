package com.barbershop.features.barbershop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "barbershops")
@ToString(exclude = {"barbers", "services", "operatingHours"})
@EqualsAndHashCode(exclude = {"barbers", "services", "operatingHours"})
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

    @Column(name = "logo_url")
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relaciones
    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.barbershop.features.barber.model.Barber> barbers;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<com.barbershop.features.service.model.Service> services;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BarbershopOperatingHours> operatingHours;
}