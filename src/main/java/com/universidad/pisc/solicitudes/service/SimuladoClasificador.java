package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.model.SugerenciaIA;
import com.universidad.pisc.solicitudes.repository.SugerenciaIARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de clasificación simulada por palabras clave.
 * Se usa como fallback o para desarrollo sin conexión.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pisc.ia.provider", havingValue = "simulado", matchIfMissing = true)
public class SimuladoClasificador implements SolicitudClasificador {

    private final TipoSolicitudRepository tipoRepository;
    private final SugerenciaIARepository sugerenciaRepository;
    private final TriageService triageService;

    @Override
    @Transactional
    public SugerenciaIA generarSugerencia(SolicitudAcademica solicitud) {
        log.info("Generando sugerencia simulada (SRP Fallback)");
        
        List<TipoSolicitud> tipos = tipoRepository.findByActivo(true);
        if (tipos.isEmpty()) return null;

        TipoSolicitud sugerido = tipos.get(0);
        String desc = solicitud.getDescripcion().toLowerCase();
        for (TipoSolicitud t : tipos) {
            if (desc.contains(t.getNombre().toLowerCase())) {
                sugerido = t;
                break;
            }
        }

        SugerenciaIA sugerencia = new SugerenciaIA();
        sugerencia.setSolicitud(solicitud);
        sugerencia.setTipoSugerido(sugerido);
        sugerencia.setPrioridadSugerida(triageService.evaluarPrioridad(solicitud));
        sugerencia.setConfianza(0.60);
        sugerencia.setJustificacionIA("Análisis básico por palabras clave (Modo SOLID Simulado).");
        
        return sugerenciaRepository.save(sugerencia);
    }
}
