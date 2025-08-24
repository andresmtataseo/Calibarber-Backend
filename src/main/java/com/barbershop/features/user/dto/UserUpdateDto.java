package com.barbershop.features.user.dto;

import com.barbershop.features.user.model.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Email(message = "El email debe tener un formato válido")
    private String email;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    private String lastName;

    @Size(max = 20, message = "El número de teléfono no puede exceder 20 caracteres")
    private String phoneNumber;

    private RoleEnum role;

    private Boolean isActive;

    @Size(max = 500, message = "La URL de la imagen de perfil no puede exceder 500 caracteres")
    private String profilePictureUrl;
}