package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.model.SugerenciaIA;
import com.universidad.pisc.solicitudes.repository.SugerenciaIARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Servicio que simula la integración con un modelo de Inteligencia Artificial
 * para la clasificación automática de solicitudes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IAService {

    private final TipoSolicitudRepository tipoRepository;
    private final SugerenciaIARepository sugerenciaRepository;
    private final TriageService triageService;
    private final Random random = new Random();

    /**
     * Analiza una solicitud y genera una sugerencia de clasificación.
     * En una implementación real, esto llamaría a un servicio de NLP (Natural Language Processing).
     */
    @Transactional
    public SugerenciaIA generarSugerencia(SolicitudAcademica solicitud) {
        log.info("Generando sugerencia de IA para la solicitud: {}", solicitud.getCodigo());

        List<TipoSolicitud> tipos = tipoRepository.findByActivo(true);
        if (tipos.isEmpty()) {
            log.warn("No hay tipos de solicitud activos para generar sugerencias.");
            return null;
        }

        // Simulación de clasificación por palabras clave
        String desc = solicitud.getDescripcion().toLowerCase();
        TipoSolicitud sugerido = tipos.get(0); // Valor por defecto

        for (TipoSolicitud t : tipos) {
            if (desc.contains(t.getNombre().toLowerCase())) {
                sugerido = t;
                break;
            }
        }

        // Usar el motor de reglas para la prioridad sugerida
        NivelPrioridad prioridadSugerida = triageService.evaluarPrioridad(solicitud);

        SugerenciaIA sugerencia = new SugerenciaIA();
        sugerencia.setSolicitud(solicitud);
        sugerencia.setTipoSugerido(sugerido);
        sugerencia.setPrioridadSugerida(prioridadSugerida);
        sugerencia.setConfianza(0.75 + (0.95 - 0.75) * random.nextDouble());
        sugerencia.setJustificacionIA("Análisis de texto detectó patrones asociados a '" + sugerido.getNombre() + "'.");
        
        return sugerenciaRepository.save(sugerencia);
    }
}
