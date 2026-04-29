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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Orquestador del ciclo de vida de solicitudes académicas.
 * Gestiona la coordinación entre el dominio, persistencia y servicios de apoyo (IA/Triage).
 */
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

    /**
     * Registra una nueva solicitud en el sistema y dispara la clasificación sugerida por IA.
     */
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
        
        SugerenciaIA sugerencia = clasificadorIA.generarSugerencia(guardada);
        guardada.setSugerenciaIA(sugerencia);

        historialService.registrarEvento(guardada, "registrarSolicitud", null, EstadoSolicitud.REGISTRADA, 
                "Solicitud registrada por el canal " + request.canal());

        solicitudRepository.flush();
        return mapper.toDetalleResponse(guardada);
    }

    /**
     * Recupera el detalle completo de una solicitud por su ID con validación de propiedad (BOLA).
     */
    @Transactional(readOnly = true)
    public SolicitudDetalleResponse obtenerSolicitud(Long id) {
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarPropiedad(solicitud);
        return mapper.toDetalleResponse(solicitud);
    }

    /**
     * Lista solicitudes filtrando según el rol del usuario (Security Filtering).
     */
    @Transactional(readOnly = true)
    public Page<SolicitudResumen> listarSolicitudes(Pageable pageable) {
        String emailActual = obtenerEmailUsuarioActual();
        
        if (esPersonalAdministrativo()) {
            return solicitudRepository.findAll(pageable).map(mapper::toResumen);
        }
        
        // Estudiantes solo ven sus propias solicitudes
        return solicitudRepository.findBySolicitanteEmail(emailActual, pageable)
                .map(mapper::toResumen);
    }

    @Transactional
    public SolicitudDetalleResponse clasificarSolicitud(Long id, ClasificarSolicitudRequest request) {
        validarRolAdministrativo();
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        TipoSolicitud tipo = tipoSolicitudRepository.findById(request.tipoSolicitudId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de solicitud no encontrado: " + request.tipoSolicitudId()));

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
        
        solicitud.clasificar(tipo, prioridad);
        
        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "clasificarSolicitud", EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA, logInfo);

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse asignarResponsable(Long id, AsignarResponsableRequest request) {
        validarRolAdministrativo();
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        Usuario responsable = usuarioRepository.findById(request.responsableId())
                .orElseThrow(() -> new EntityNotFoundException("Responsable no encontrado"));

        solicitud.asignar(responsable);
        solicitud.getAsignaciones().forEach(a -> a.setActiva(false));

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setSolicitud(solicitud);
        nuevaAsignacion.setResponsable(responsable);
        nuevaAsignacion.setAsignadoPor(historialService.obtenerUsuarioActualParaAsignacion(solicitud.getSolicitante()));
        nuevaAsignacion.setNotas(request.notas());
        
        solicitud.getAsignaciones().add(nuevaAsignacion);

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "asignarResponsable", estadoAnterior, EstadoSolicitud.EN_ATENCION, 
                "Responsable: " + responsable.getNombre());

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse marcarAtendida(Long id, MarcarAtendidaRequest request) {
        validarRolAdministrativo();
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        solicitud.marcarAtendida(request.observacionResolucion());

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "marcarAtendida", EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA, 
                "Resolución: " + request.observacionResolucion());

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse cerrarSolicitud(Long id, CerrarSolicitudRequest request) {
        validarRolAdministrativo();
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        solicitud.cerrar(request.observacionCierre());

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "cerrarSolicitud", EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA, 
                "Cierre formal.");

        return mapper.toDetalleResponse(actualizada);
    }

    @Transactional
    public SolicitudDetalleResponse rechazarSolicitud(Long id, RechazarSolicitudRequest request) {
        validarRolAdministrativo();
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        solicitud.rechazar(request.motivo(), request.justificacion());

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "rechazarSolicitud", estadoAnterior, EstadoSolicitud.RECHAZADA, 
                "Motivo: " + request.motivo());

        return mapper.toDetalleResponse(actualizada);
    }

    private SolicitudAcademica obtenerEntidad(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Solicitud no encontrada con id: " + id));
    }

    /**
     * Centraliza la validación de propiedad para cumplir con OWASP BOLA.
     */
    private void validarPropiedad(SolicitudAcademica solicitud) {
        if (!esPersonalAdministrativo() && !solicitud.getSolicitante().getEmail().equals(obtenerEmailUsuarioActual())) {
            throw new com.universidad.pisc.config.BusinessException(
                "Acceso denegado: Esta solicitud no le pertenece.", 
                org.springframework.http.HttpStatus.FORBIDDEN
            );
        }
    }

    private void validarRolAdministrativo() {
        if (!esPersonalAdministrativo()) {
            throw new com.universidad.pisc.config.BusinessException(
                "Acceso denegado: Solo el personal administrativo puede realizar esta acción.", 
                org.springframework.http.HttpStatus.FORBIDDEN
            );
        }
    }

    private boolean esPersonalAdministrativo() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATIVO") || 
                               a.getAuthority().equals("ROLE_DIRECTOR") || 
                               a.getAuthority().equals("ROLE_COORDINADOR"));
    }

    private String obtenerEmailUsuarioActual() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validarVersion(SolicitudAcademica solicitud, Long version) {
        if (!solicitud.getVersion().equals(version)) {
            throw new com.universidad.pisc.config.BusinessException("Conflicto de concurrencia: la solicitud fue modificada por otro usuario.", org.springframework.http.HttpStatus.CONFLICT);
        }
    }
}
