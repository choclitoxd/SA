package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.catalogo.dto.TipoSolicitudResponse;
import com.universidad.pisc.identidad.dto.UsuarioResumen;
import com.universidad.pisc.solicitudes.model.CanalOrigen;
import com.universidad.pisc.solicitudes.model.EstadoSolicitud;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SolicitudDetalleResponse(
    Long id,
    String codigo,
    String descripcion,
    CanalOrigen canal,
    EstadoSolicitud estado,
    TipoSolicitudResponse tipo,
    PrioridadResponse prioridad,
    UsuarioResumen solicitante,
    UsuarioResumen responsableActual,
    Integer contadorReaperturas,
    LocalDateTime fechaRegistro,
    LocalDateTime fechaUltimaActualizacion,
    LocalDate fechaLimite,
    String observacionCierre,
    SugerenciaIAResponse sugerenciaIA,
    Long version
) {}
