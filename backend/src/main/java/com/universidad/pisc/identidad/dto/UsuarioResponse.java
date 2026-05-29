package com.universidad.pisc.identidad.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.universidad.pisc.identidad.enums.NombreRol;

public record UsuarioResponse(
    Long id,
    String nombre,
    String apellido,
    String email,
    String identificacion,
    Set<NombreRol> roles,
    boolean activo,
    LocalDateTime creadoEn
) {}
