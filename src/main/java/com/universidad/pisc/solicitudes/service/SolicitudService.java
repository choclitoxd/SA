package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import com.universidad.pisc.solicitudes.dto.*;
import com.universidad.pisc.solicitudes.enums.*;
import com.universidad.pisc.solicitudes.model.*;
import com.universidad.pisc.solicitudes.repository.SolicitudAcademicaRepository;
import com.universidad.pisc.solicitudes.repository.SugerenciaIARepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudAcademicaRepository solicitudRepository;
    private final SolicitudHistorialService historialService;
    private final UsuarioRepository usuarioRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final TriageService triageService;
    private final SolicitudClasificador clasificadorIA;
    private final SugerenciaIARepository sugerenciaIARepository;
    private final SolicitudMapper mapper;

    @Transactional
    public SolicitudDetalleResponse registrarSolicitud(RegistrarSolicitudRequest request) {
        Usuario solicitante = usuarioRepository.findByIdentificacion(request.solicitanteId())
                .orElseThrow(() -> new EntityNotFoundException("Solicitante no encontrado con identificación: " + request.solicitanteId()));

        SolicitudAcademica solicitud = new SolicitudAcademica();
        solicitud.setDescripcion(request.descripcion());
        solicitud.setCanal(request.canal());
        solicitud.setSolicitante(solicitante);
        if (request.fechaLimite() != null) {
            solicitud.setFechaLimite(request.fechaLimite().atStartOfDay());
        }

        SolicitudAcademica guardada = solicitudRepository.save(solicitud);
        
        // Generar sugerencia de IA
        SugerenciaIA sugerencia = clasificadorIA.generarSugerencia(guardada);
        guardada.setSugerenciaIA(sugerencia);

        historialService.registrarEvento(guardada, "registrarSolicitud", null, EstadoSolicitud.REGISTRADA, 
                "Solicitud registrada por el canal " + request.canal());

        solicitudRepository.flush();
        return mapper.toDetalleResponse(guardada);
    }

    @Transactional(readOnly = true)
    public SolicitudDetalleResponse obtenerSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .map(mapper::toDetalleResponse)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<SolicitudResumen> listarSolicitudes(Pageable pageable) {
        return solicitudRepository.findAll(pageable).map(mapper::toResumen);
    }

    @Transactional
    public SolicitudDetalleResponse clasificarSolicitud(Long id, ClasificarSolicitudRequest request) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());
        validarEstado(solicitud, EstadoSolicitud.REGISTRADA);

        TipoSolicitud tipo = tipoSolicitudRepository.findById(request.tipoSolicitudId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de solicitud no encontrado: " + request.tipoSolicitudId()));

        solicitud.setTipo(tipo);

        if (request.sugerenciaIaId() != null) {
            SugerenciaIA sugerencia = sugerenciaIARepository.findById(request.sugerenciaIaId())
                    .orElseThrow(() -> new EntityNotFoundException("Sugerencia de IA no encontrada: " + request.sugerenciaIaId()));
            sugerencia.setConfirmada(true);
            sugerencia.setAjustada(!sugerencia.getTipoSugerido().getId().equals(tipo.getId()));
            sugerenciaIARepository.save(sugerencia);
        }

        Prioridad prioridad;
        String logInfo;
        if (request.nivelPrioridad() != null) {
            prioridad = new Prioridad(request.nivelPrioridad(), request.justificacionPrioridad(), LocalDateTime.now());
            logInfo = "Clasificada manualmente como " + tipo.getNombre();
        } else {
            NivelPrioridad nivelAuto = triageService.evaluarPrioridad(solicitud);
            prioridad = new Prioridad(nivelAuto, "Triage automático", LocalDateTime.now());
            logInfo = "Clasificada automáticamente como " + tipo.getNombre();
        }
        
        solicitud.setPrioridad(prioridad);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        
        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "clasificarSolicitud", EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, logInfo);

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse asignarResponsable(Long id, AsignarResponsableRequest request) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());
        validarEstado(solicitud, EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        Usuario responsable = usuarioRepository.findById(request.responsableId())
                .orElseThrow(() -> new EntityNotFoundException("Responsable no encontrado"));

        solicitud.getAsignaciones().forEach(a -> a.setActiva(false));

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setSolicitud(solicitud);
        nuevaAsignacion.setResponsable(responsable);
        nuevaAsignacion.setAsignadoPor(historialService.obtenerUsuarioActualParaAsignacion(solicitud.getSolicitante()));
        nuevaAsignacion.setNotas(request.notas());
        
        solicitud.getAsignaciones().add(nuevaAsignacion);
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "asignarResponsable", estadoAnterior, EstadoSolicitud.EN_ATENCION, 
                "Responsable: " + responsable.getNombre());

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse marcarAtendida(Long id, MarcarAtendidaRequest request) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());
        validarEstado(solicitud, EstadoSolicitud.EN_ATENCION);

        solicitud.setObservacionResolucion(request.observacionResolucion());
        solicitud.setEstado(EstadoSolicitud.ATENDIDA);

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "marcarAtendida", EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA, 
                "Resolución: " + request.observacionResolucion());

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse cerrarSolicitud(Long id, CerrarSolicitudRequest request) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());
        validarEstado(solicitud, EstadoSolicitud.ATENDIDA);

        solicitud.setObservacionCierre(request.observacionCierre());
        solicitud.setEstado(EstadoSolicitud.CERRADA);

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "cerrarSolicitud", EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA, 
                "Cierre formal.");

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse rechazarSolicitud(Long id, RechazarSolicitudRequest request) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());
        validarEstado(solicitud, EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.setMotivoRechazo(request.motivo());
        solicitud.setObservacionResolucion(request.justificacion());
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "rechazarSolicitud", estadoAnterior, EstadoSolicitud.RECHAZADA, 
                "Motivo: " + request.motivo());

        return mapper.toDetalleResponse(actualizada);
    }

    private SolicitudAcademica obtenerEntidad(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada con id: " + id));
    }

    private void validarVersion(SolicitudAcademica solicitud, Long version) {
        if (!solicitud.getVersion().equals(version)) {
            throw new IllegalStateException("Conflicto de concurrencia: la versión proporcionada no coincide");
        }
    }

    private void validarEstado(SolicitudAcademica solicitud, EstadoSolicitud... estadosPermitidos) {
        java.util.EnumSet<EstadoSolicitud> permits = java.util.EnumSet.of(estadosPermitidos[0], estadosPermitidos);
        if (!permits.contains(solicitud.getEstado())) {
            throw new IllegalStateException("Transición de estado no permitida desde: " + solicitud.getEstado());
        }
    }
}
