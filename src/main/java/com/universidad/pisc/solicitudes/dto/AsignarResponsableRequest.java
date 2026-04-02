package com.universidad.pisc.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AsignarResponsableRequest(
    @NotNull(message = "El ID del responsable es obligatorio")
    Long responsableId,

    @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
    String notas,

    @NotNull(message = "La versión del recurso es obligatoria")
    Long version
) {}
