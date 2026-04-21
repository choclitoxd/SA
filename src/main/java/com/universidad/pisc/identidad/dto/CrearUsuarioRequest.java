package com.universidad.pisc.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

import com.universidad.pisc.identidad.enums.NombreRol;

public record CrearUsuarioRequest(
    @NotNull(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @NotNull(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    String apellido,

    @NotNull(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    String email,

    @NotNull(message = "La identificación es obligatoria")
    @Pattern(regexp = "^\\d{7,10}$", message = "La identificación debe contener entre 7 y 10 dígitos")
    String identificacion,

    @NotEmpty(message = "El usuario debe tener al menos un rol")
    Set<NombreRol> roles,
    
    // NOTA: La especificación OpenAPI no incluye el campo password, pero es necesario para crear un usuario.
    // Lo incluyo aquí asumiendo que es un requisito implícito para la creación.
    @NotNull(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password
) {}
