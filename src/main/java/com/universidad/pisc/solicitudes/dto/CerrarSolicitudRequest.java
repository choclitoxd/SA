package com.universidad.pisc.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CerrarSolicitudRequest(
    @NotNull(message = "La observación de cierre es obligatoria")
    @Size(min = 20, max = 1000, message = "La observación debe tener entre 20 y 1000 caracteres")
    String observacionCierre,

    @NotNull(message = "La versión del recurso es obligatoria")
    Long version
) {}
