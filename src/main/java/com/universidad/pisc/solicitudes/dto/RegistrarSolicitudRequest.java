package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.solicitudes.model.CanalOrigen;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegistrarSolicitudRequest(
    @NotNull(message = "La descripción es obligatoria")
    @Size(min = 30, max = 2000, message = "La descripción debe tener entre 30 y 2000 caracteres")
    String descripcion,

    @NotNull(message = "El canal es obligatorio")
    CanalOrigen canal,

    @NotNull(message = "El solicitanteId es obligatorio")
    @Pattern(regexp = "^\\d{7,15}$", message = "El identificador del solicitante debe contener entre 7 y 15 dígitos")
    String solicitanteId,

    LocalDate fechaLimite
) {}
