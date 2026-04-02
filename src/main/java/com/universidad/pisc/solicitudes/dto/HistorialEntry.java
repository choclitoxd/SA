package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.identidad.dto.UsuarioResumen;
import com.universidad.pisc.solicitudes.model.EstadoSolicitud;
import java.time.LocalDateTime;

public record HistorialEntry(
    Long id,
    LocalDateTime fechaHora,
    String accionRealizada,
    EstadoSolicitud estadoAnterior,
    EstadoSolicitud estadoNuevo,
    String observaciones,
    UsuarioResumen usuarioResponsable
) {}
