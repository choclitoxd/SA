package com.universidad.pisc.identidad.dto;

import com.universidad.pisc.identidad.model.NombreRol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

// Para la actualización, los campos no son obligatorios, permitiendo cambios parciales.
public record ActualizarUsuarioRequest(
    @Size(min = 2, max = 100)
    String nombre,

    @Size(min = 2, max = 100)
    String apellido,

    @Email
    String email,

    @NotEmpty // Se podría permitir un conjunto vacío si la lógica lo permitiera, pero 'NotEmpty' es más seguro.
    Set<NombreRol> roles
) {}
