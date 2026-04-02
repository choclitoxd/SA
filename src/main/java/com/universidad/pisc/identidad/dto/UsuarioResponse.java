package com.universidad.pisc.identidad.dto;

import com.universidad.pisc.identidad.model.NombreRol;
import java.time.LocalDateTime;
import java.util.Set;

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
