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

@Slf4j
@Service
@RequiredArgsConstructor
public class TriageService {

    private final ReglaPrioridadRepository reglaRepository;
    private final ExpressionParser parser = new SpelExpressionParser();

    public NivelPrioridad evaluarPrioridad(SolicitudAcademica solicitud) {
        List<ReglaPrioridad> reglasActivas = reglaRepository.findAll().stream()
                .filter(ReglaPrioridad::getActiva)
                .sorted(Comparator.comparing(ReglaPrioridad::getPeso).reversed())
                .toList();

        StandardEvaluationContext context = new StandardEvaluationContext(solicitud);
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
        return NivelPrioridad.BAJA;
    }
}
