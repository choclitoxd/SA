package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import com.universidad.pisc.solicitudes.dto.*;
import com.universidad.pisc.solicitudes.enums.*;
import com.universidad.pisc.solicitudes.model.*;
import com.universidad.pisc.solicitudes.repository.HistorialSolicitudRepository;
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
    private final HistorialSolicitudRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final TriageService triageService;
    private final IAService iaService;
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
        
        // Generar sugerencia de IA automáticamente
        iaService.generarSugerencia(guardada);

        registrarHistorial(guardada, "registrarSolicitud", null, EstadoSolicitud.REGISTRADA, 
                "Solicitud registrada por el canal " + request.canal());

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
            logInfo = "Solicitud clasificada manualmente como " + tipo.getNombre();
        } else {
            NivelPrioridad nivelAuto = triageService.evaluarPrioridad(solicitud);
            prioridad = new Prioridad(nivelAuto, "Asignada automáticamente por el motor de reglas", LocalDateTime.now());
            logInfo = "Solicitud clasificada automáticamente (Triage) como " + tipo.getNombre() + " con prioridad " + nivelAuto;
        }
        
        solicitud.setPrioridad(prioridad);
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        
        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        
        registrarHistorial(actualizada, "clasificarSolicitud", EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, 
                logInfo);

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse asignarResponsable(Long id, AsignarResponsableRequest request) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        
        validarVersion(solicitud, request.version());
        validarEstado(solicitud, EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();

        Usuario responsable = usuarioRepository.findById(request.responsableId())
                .orElseThrow(() -> new EntityNotFoundException("Responsable no encontrado con id: " + request.responsableId()));

        if (!responsable.getActivo()) {
            throw new IllegalStateException("El responsable asignado debe estar activo");
        }

        solicitud.getAsignaciones().forEach(a -> a.setActiva(false));

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setSolicitud(solicitud);
        nuevaAsignacion.setResponsable(responsable);
        nuevaAsignacion.setAsignadoPor(obtenerUsuarioActual(solicitud.getSolicitante()));
        nuevaAsignacion.setNotas(request.notas());
        
        solicitud.getAsignaciones().add(nuevaAsignacion);
        solicitud.setEstado(EstadoSolicitud.EN_ATENCION);

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        
        registrarHistorial(actualizada, "asignarResponsable", estadoAnterior, EstadoSolicitud.EN_ATENCION, 
                "Responsable asignado: " + responsable.getNombre() + " " + responsable.getApellido());

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
        
        registrarHistorial(actualizada, "marcarAtendida", EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA, 
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
        
        registrarHistorial(actualizada, "cerrarSolicitud", EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA, 
                "Cierre formal: " + request.observacionCierre());

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
        
        registrarHistorial(actualizada, "rechazarSolicitud", estadoAnterior, EstadoSolicitud.RECHAZADA, 
                "Motivo: " + request.motivo() + ". Justificación: " + request.justificacion());

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

    private void registrarHistorial(SolicitudAcademica solicitud, String accion, 
                                   EstadoSolicitud anterior, EstadoSolicitud nuevo, 
                                   String obs) {
        Usuario ejecutor = obtenerUsuarioActual(solicitud.getSolicitante());

        HistorialSolicitud h = new HistorialSolicitud();
        h.setSolicitud(solicitud);
        h.setAccionRealizada(accion);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setObservaciones(obs);
        h.setEjecutadaPor(ejecutor);
        historialRepository.save(h);
    }

    private Usuario obtenerUsuarioActual(Usuario fallback) {
        org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null && !auth.getPrincipal().equals("anonymousUser")) {
            return usuarioRepository.findByEmail(auth.getName()).orElse(fallback);
        }
        return fallback;
    }
}
