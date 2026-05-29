package com.universidad.pisc.identidad.dto;

public record UsuarioResumen(
    Long id,
    String nombre,
    String apellido,
    String identificacion,
    boolean activo
) {}
