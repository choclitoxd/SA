package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.model.SugerenciaIA;

/**
 * Interface que define el contrato para cualquier motor de clasificación 
 * inteligente de solicitudes. (Cumple con DIP y OCP).
 */
public interface SolicitudClasificador {
    
    /**
     * Analiza el texto de una solicitud y genera una sugerencia técnica.
     * @param solicitud La solicitud académica a analizar.
     * @return Un objeto SugerenciaIA con el resultado del análisis.
     */
    SugerenciaIA generarSugerencia(SolicitudAcademica solicitud);
}
