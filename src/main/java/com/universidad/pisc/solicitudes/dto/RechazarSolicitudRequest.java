package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.solicitudes.enums.MotivoRechazo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RechazarSolicitudRequest(
    @NotNull(message = "El motivo de rechazo es obligatorio")
    MotivoRechazo motivo,

    @NotNull(message = "La justificación del rechazo es obligatoria")
    @Size(min = 20, max = 1000, message = "La justificación debe tener entre 20 y 1000 caracteres")
    String justificacion,

    @NotNull(message = "La versión del recurso es obligatoria")
    Long version
) {}
