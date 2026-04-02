package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.model.NivelPrioridad;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TriageService {

    private final ReglaPrioridadRepository reglaRepository;
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluates all active rules against a request and returns the resulting priority level.
     * If multiple rules match, the one with the highest weight wins.
     */
    public NivelPrioridad evaluarPrioridad(SolicitudAcademica solicitud) {
        List<ReglaPrioridad> reglasActivas = reglaRepository.findAll().stream()
                .filter(ReglaPrioridad::getActiva)
                .sorted(Comparator.comparing(ReglaPrioridad::getPeso).reversed())
                .toList();

        StandardEvaluationContext context = new StandardEvaluationContext(solicitud);
        // Expose helper variables if needed, like days remaining
        // context.setVariable("diasRestantes", ...);

        for (ReglaPrioridad regla : reglasActivas) {
            try {
                Boolean matches = parser.parseExpression(regla.getCondicion()).getValue(context, Boolean.class);
                if (Boolean.TRUE.equals(matches)) {
                    log.info("Rule matched: {} - Resulting priority: {}", regla.getNombre(), regla.getNivelResultante());
                    return regla.getNivelResultante();
                }
            } catch (Exception e) {
                log.error("Error evaluating rule '{}': {}", regla.getNombre(), e.getMessage());
            }
        }

        // Default priority if no rule matches
        return NivelPrioridad.BAJA;
    }
}
