package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClasificarSolicitudRequest(
    @NotNull(message = "El ID del tipo de solicitud es obligatorio")
    Long tipoSolicitudId,

    NivelPrioridad nivelPrioridad,

    @Size(max = 500, message = "La justificación debe tener máximo 500 caracteres")
    String justificacionPrioridad,

    Long sugerenciaIaId,

    @NotNull(message = "La versión del recurso es obligatoria para el control de concurrencia")
    Long version
) {}
