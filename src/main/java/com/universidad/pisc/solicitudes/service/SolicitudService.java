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
     * @return Detalle de la solicitud con el código generado y sugerencia de IA inicial.
     */
    @Transactional
    public SolicitudDetalleResponse registrarSolicitud(RegistrarSolicitudRequest request) {
        // ... (resto del código igual)
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

    /**
     * Recupera el detalle completo de una solicitud por su ID.
     */
    @Transactional(readOnly = true)
    public SolicitudDetalleResponse obtenerSolicitud(Long id) {
        return mapper.toDetalleResponse(obtenerEntidad(id));
    }

    /**
        * Lista solicitudes de forma paginada para la vista de resumen.
    */
    @Transactional(readOnly = true)
    public Page<SolicitudResumen> listarSolicitudes(Pageable pageable) {
        return solicitudRepository.findAll(pageable)
                .map(mapper::toResumen);
    }

    /**
        * Ejecuta la transición a CLASIFICADA asignando tipo y prioridad (manual o vía motor de reglas).
    */
    @Transactional
    public SolicitudDetalleResponse clasificarSolicitud(Long id, ClasificarSolicitudRequest request) {
        // ... (resto del código igual)
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

    /**
     * Asigna un responsable y mueve la solicitud a EN_ATENCION, gestionando la trazabilidad de asignaciones.
     */
    @Transactional
    public SolicitudDetalleResponse asignarResponsable(Long id, AsignarResponsableRequest request) {
        // ... (resto del código igual)
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

    /**
     * Registra la solución técnica de la solicitud. Requiere estado EN_ATENCION.
     */
    @Transactional
    public SolicitudDetalleResponse marcarAtendida(Long id, MarcarAtendidaRequest request) {
        // ... (resto del código igual)
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        solicitud.marcarAtendida(request.observacionResolucion());

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "marcarAtendida", EstadoSolicitud.EN_ATENCION, EstadoSolicitud.ATENDIDA, 
                "Resolución: " + request.observacionResolucion());

        return mapper.toDetalleResponse(actualizada);
    }

    /**
     * Cierre formal de la solicitud tras validación de la atención. Bloquea cambios posteriores.
     */
    @Transactional
    public SolicitudDetalleResponse cerrarSolicitud(Long id, CerrarSolicitudRequest request) {
        // ... (resto del código igual)
        SolicitudAcademica solicitud = obtenerEntidad(id);
        validarVersion(solicitud, request.version());

        solicitud.cerrar(request.observacionCierre());

        SolicitudAcademica actualizada = solicitudRepository.save(solicitud);
        historialService.registrarEvento(actualizada, "cerrarSolicitud", EstadoSolicitud.ATENDIDA, EstadoSolicitud.CERRADA, 
                "Cierre formal.");

        return mapper.toDetalleResponse(actualizada);
    }

    /**
     * Cancela la solicitud registrando el motivo administrativo del rechazo.
     */
    @Transactional
    public SolicitudDetalleResponse rechazarSolicitud(Long id, RechazarSolicitudRequest request) {
        // ... (resto del código igual)
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
     * Garantiza la integridad de datos mediante bloqueo optimista.
     * @throws BusinessException si la versión del cliente es obsoleta (HTTP 409).
     */
    private void validarVersion(SolicitudAcademica solicitud, Long version) {
        if (!solicitud.getVersion().equals(version)) {
            throw new com.universidad.pisc.config.BusinessException("Conflicto de concurrencia: la solicitud fue modificada por otro usuario.", org.springframework.http.HttpStatus.CONFLICT);
        }
    }
}
