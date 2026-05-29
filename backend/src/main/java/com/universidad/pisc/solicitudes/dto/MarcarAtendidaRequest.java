package com.universidad.pisc.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MarcarAtendidaRequest(
    @NotNull(message = "La observación de resolución es obligatoria")
    @Size(min = 20, max = 2000, message = "La observación debe tener entre 20 y 2000 caracteres")
    String observacionResolucion,

    @NotNull(message = "La versión del recurso es obligatoria")
    Long version
) {}
