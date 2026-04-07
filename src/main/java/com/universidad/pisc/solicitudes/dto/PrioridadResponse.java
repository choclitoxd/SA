package com.universidad.pisc.solicitudes.dto;

import java.time.LocalDateTime;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;

public record PrioridadResponse(
    NivelPrioridad nivel,
    String justificacion,
    LocalDateTime asignadaEn
) {}
