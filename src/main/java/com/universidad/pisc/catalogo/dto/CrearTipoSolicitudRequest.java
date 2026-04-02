package com.universidad.pisc.catalogo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearTipoSolicitudRequest(
    @NotNull @Size(min = 3, max = 100)
    String nombre,

    @Size(max = 500)
    String descripcion,

    @NotNull @Min(1) @Max(90)
    Integer tiempoAtencionDias
) {}
