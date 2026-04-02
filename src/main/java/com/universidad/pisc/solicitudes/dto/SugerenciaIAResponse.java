package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.catalogo.dto.TipoSolicitudResponse;
import com.universidad.pisc.catalogo.model.NivelPrioridad;
import java.time.LocalDateTime;

public record SugerenciaIAResponse(
    Long id,
    TipoSolicitudResponse tipoSugerido,
    NivelPrioridad prioridadSugerida,
    String justificacionIA,
    Double confianza,
    Boolean confirmada,
    Boolean ajustada,
    LocalDateTime generadaEn
) {}
