package com.universidad.pisc.solicitudes.dto;

import com.universidad.pisc.catalogo.dto.TipoSolicitudMapper;
import com.universidad.pisc.identidad.dto.UsuarioMapper;
import com.universidad.pisc.solicitudes.model.HistorialSolicitud;
import com.universidad.pisc.solicitudes.model.Prioridad;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.model.SugerenciaIA;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolicitudMapper {

    private final TipoSolicitudMapper tipoSolicitudMapper;
    private final UsuarioMapper usuarioMapper;

    public SolicitudDetalleResponse toDetalleResponse(SolicitudAcademica solicitud) {
        if (solicitud == null) {
            return null;
        }

        return new SolicitudDetalleResponse(
                solicitud.getId(),
                solicitud.getCodigo(),
                solicitud.getDescripcion(),
                solicitud.getCanal(),
                solicitud.getEstado(),
                tipoSolicitudMapper.toResponse(solicitud.getTipo()),
                toPrioridadResponse(solicitud.getPrioridad()),
                usuarioMapper.toResumen(solicitud.getSolicitante()),
                usuarioMapper.toResumen(solicitud.getResponsableActual()),
                solicitud.getContadorReaperturas(),
                solicitud.getFechaRegistro(),
                solicitud.getFechaUltimaActualizacion(),
                solicitud.getFechaLimite() != null ? solicitud.getFechaLimite().toLocalDate() : null,
                solicitud.getObservacionCierre(),
                toSugerenciaResponse(solicitud.getSugerenciaIA()),
                solicitud.getVersion()
        );
    }

    public SolicitudResumen toResumen(SolicitudAcademica solicitud) {
        if (solicitud == null) {
            return null;
        }
        
        String tipoNombre = solicitud.getTipo() != null ? solicitud.getTipo().getNombre() : null;
        var prioridadNivel = solicitud.getPrioridad() != null ? solicitud.getPrioridad().getNivel() : null;
        String solicitanteNombre = solicitud.getSolicitante().getNombre() + " " + solicitud.getSolicitante().getApellido();
        var responsable = solicitud.getResponsableActual();
        String responsableNombre = responsable != null ? responsable.getNombre() + " " + responsable.getApellido() : null;

        return new SolicitudResumen(
                solicitud.getId(),
                solicitud.getCodigo(),
                solicitud.getEstado(),
                tipoNombre,
                prioridadNivel,
                solicitanteNombre,
                responsableNombre,
                solicitud.getFechaRegistro(),
                solicitud.getFechaLimite() != null ? solicitud.getFechaLimite().toLocalDate() : null,
                solicitud.isVencida()
        );
    }

    public PrioridadResponse toPrioridadResponse(Prioridad prioridad) {
        if (prioridad == null) {
            return null;
        }
        return new PrioridadResponse(
                prioridad.getNivel(),
                prioridad.getJustificacion(),
                prioridad.getAsignadaEn()
        );
    }

    public SugerenciaIAResponse toSugerenciaResponse(SugerenciaIA sugerenciaIA) {
        if (sugerenciaIA == null) {
            return null;
        }
        return new SugerenciaIAResponse(
                sugerenciaIA.getId(),
                tipoSolicitudMapper.toResponse(sugerenciaIA.getTipoSugerido()),
                sugerenciaIA.getPrioridadSugerida(),
                sugerenciaIA.getJustificacionIA(),
                sugerenciaIA.getConfianza(),
                sugerenciaIA.getConfirmada(),
                sugerenciaIA.getAjustada(),
                sugerenciaIA.getGeneradaEn()
        );
    }

    public HistorialEntry toHistorialEntry(HistorialSolicitud historial) {
        if (historial == null) {
            return null;
        }
        return new HistorialEntry(
                historial.getId(),
                historial.getFechaHora(),
                historial.getAccionRealizada(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getObservaciones(),
                usuarioMapper.toResumen(historial.getEjecutadaPor())
        );
    }
}
