package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.catalogo.model.NivelPrioridad;
import com.universidad.pisc.solicitudes.model.EstadoSolicitud;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudResumen(
    Long id,
    String codigo,
    EstadoSolicitud estado,
    String tipoNombre,
    NivelPrioridad prioridad,
    String solicitanteNombre,
    String responsableNombre,
    LocalDateTime fechaRegistro,
    LocalDate fechaLimite,
    boolean vencida
) {}
