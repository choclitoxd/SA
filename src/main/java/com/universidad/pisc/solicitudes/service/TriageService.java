package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import com.universidad.pisc.catalogo.repository.ReglaPrioridadRepository;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Servicio encargado del Triage automatizado de solicitudes académicas.
 * 
 * Este servicio actúa como un motor de reglas que evalúa las condiciones dinámicas
 * definidas en el catálogo de reglas de prioridad para determinar qué nivel de 
 * prioridad debe asignarse a una solicitud específica.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriageService {

    private final ReglaPrioridadRepository reglaRepository;
    
    /**
     * Parser de Spring Expression Language (SpEL) utilizado para evaluar
     * las cadenas de texto que contienen las condiciones de las reglas.
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evalúa todas las reglas de prioridad activas contra una solicitud dada.
     * 
     * El proceso sigue estos pasos:
     * 1. Obtiene todas las reglas marcadas como activas en la base de datos.
     * 2. Las ordena por 'peso' de forma descendente (las reglas más importantes se evalúan primero).
     * 3. Ejecuta cada condición SpEL usando la solicitud como contexto de evaluación.
     * 4. Retorna el nivel de prioridad de la primera regla que coincida.
     *
     * @param solicitud La solicitud académica a evaluar.
     * @return El NivelPrioridad resultante de la evaluación, o BAJA por defecto si ninguna regla coincide.
     */
    public NivelPrioridad evaluarPrioridad(SolicitudAcademica solicitud) {
        log.debug("Iniciando triage automático para la solicitud: {}", solicitud.getCodigo());

        // Paso 1 y 2: Recuperar y ordenar reglas por relevancia (peso)
        List<ReglaPrioridad> reglasActivas = reglaRepository.findAll().stream()
                .filter(ReglaPrioridad::getActiva)
                .sorted(Comparator.comparing(ReglaPrioridad::getPeso).reversed())
                .toList();

        // Paso 3: Crear el contexto de evaluación apuntando al objeto solicitud
        // Esto permite que las reglas usen campos como 'tipoNombre' o 'diasRestantes'
        StandardEvaluationContext context = new StandardEvaluationContext(solicitud);

        for (ReglaPrioridad regla : reglasActivas) {
            try {
                // Evaluar la expresión booleana de la regla
                Boolean coincide = parser.parseExpression(regla.getCondicion()).getValue(context, Boolean.class);
                
                if (Boolean.TRUE.equals(coincide)) {
                    log.info("Regla aplicada: '{}' -> Prioridad asignada: {}", 
                            regla.getNombre(), regla.getNivelResultante());
                    return regla.getNivelResultante();
                }
            } catch (Exception e) {
                // Si una regla está mal escrita, se loguea el error y se continúa con la siguiente
                log.error("Error crítico evaluando la regla '{}': {}", regla.getNombre(), e.getMessage());
            }
        }

        // Paso 4: Valor por defecto si no se cumplen condiciones especiales
        log.info("No se encontraron reglas coincidentes para la solicitud {}. Asignando prioridad BAJA por defecto.", 
                solicitud.getCodigo());
        return NivelPrioridad.BAJA;
    }
}
