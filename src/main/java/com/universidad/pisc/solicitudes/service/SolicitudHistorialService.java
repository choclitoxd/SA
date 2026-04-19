package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import com.universidad.pisc.solicitudes.enums.EstadoSolicitud;
import com.universidad.pisc.solicitudes.model.HistorialSolicitud;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.repository.HistorialSolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio especializado en el registro del historial de auditoría de solicitudes.
 * Cumple con el Principio de Responsabilidad Única (SRP).
 */
@Service
@RequiredArgsConstructor
public class SolicitudHistorialService {

    private final HistorialSolicitudRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra un evento en el historial de la solicitud.
     * Usa propagación REQUIRED para unirse a la transacción actual.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrarEvento(SolicitudAcademica solicitud, String accion, 
                                EstadoSolicitud anterior, EstadoSolicitud nuevo, 
                                String observaciones) {
        
        Usuario ejecutor = obtenerUsuarioAutenticado(solicitud.getSolicitante());

        HistorialSolicitud h = new HistorialSolicitud();
        h.setSolicitud(solicitud);
        h.setAccionRealizada(accion);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setObservaciones(observaciones);
        h.setEjecutadaPor(ejecutor);
        
        historialRepository.save(h);
    }

    /**
     * Obtiene el usuario actual para procesos de asignación de negocio.
     */
    public Usuario obtenerUsuarioActualParaAsignacion(Usuario fallback) {
        return obtenerUsuarioAutenticado(fallback);
    }

    /**
     * Obtiene el usuario real desde el contexto de seguridad de Spring.
     */
    private Usuario obtenerUsuarioAutenticado(Usuario fallback) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null 
                && !auth.getPrincipal().equals("anonymousUser")) {
            return usuarioRepository.findByEmail(auth.getName()).orElse(fallback);
        }
        return fallback;
    }
}
