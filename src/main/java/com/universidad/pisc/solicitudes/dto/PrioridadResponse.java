package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.catalogo.model.NivelPrioridad;
import java.time.LocalDateTime;

public record PrioridadResponse(
    NivelPrioridad nivel,
    String justificacion,
    LocalDateTime asignadaEn
) {}
