package com.universidad.pisc.catalogo.dto;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;

/**
 * DTO de respuesta para Regla de Prioridad.
 */
public record ReglaPrioridadResponse(
    Long id,
    String nombre,
    String descripcion,
    String condicion,
    NivelPrioridad nivelResultante,
    Integer peso,
    Boolean activa,
    Long version
) {}
