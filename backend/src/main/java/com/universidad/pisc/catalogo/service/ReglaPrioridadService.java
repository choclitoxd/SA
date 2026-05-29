package com.universidad.pisc.catalogo.service;

import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import com.universidad.pisc.catalogo.repository.ReglaPrioridadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio encargado de la lógica de negocio de las Reglas de Prioridad.
 * Cumple con SRP al separar la gestión de reglas de la capa de transporte (Controller).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReglaPrioridadService {

    private final ReglaPrioridadRepository reglaRepository;

    @Transactional
    public ReglaPrioridad crearRegla(ReglaPrioridad regla) {
        log.info("Creando nueva regla de prioridad: {}", regla.getNombre());
        return reglaRepository.save(regla);
    }

    @Transactional
    public ReglaPrioridad actualizarRegla(Long id, ReglaPrioridad datosNuevos) {
        log.info("Actualizando regla de prioridad con ID: {}", id);
        return reglaRepository.findById(id)
                .map(reglaExistente -> {
                    // Lógica de actualización parcial
                    if (datosNuevos.getNombre() != null) reglaExistente.setNombre(datosNuevos.getNombre());
                    if (datosNuevos.getDescripcion() != null) reglaExistente.setDescripcion(datosNuevos.getDescripcion());
                    if (datosNuevos.getCondicion() != null) reglaExistente.setCondicion(datosNuevos.getCondicion());
                    if (datosNuevos.getPeso() != null) reglaExistente.setPeso(datosNuevos.getPeso());
                    if (datosNuevos.getNivelResultante() != null) reglaExistente.setNivelResultante(datosNuevos.getNivelResultante());
                    if (datosNuevos.getActiva() != null) reglaExistente.setActiva(datosNuevos.getActiva());
                    
                    return reglaRepository.save(reglaExistente);
                })
                .orElseThrow(() -> new EntityNotFoundException("Regla de prioridad no encontrada con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ReglaPrioridad> listarReglasActivas() {
        return reglaRepository.findByActivaTrueOrderByPesoDesc();
    }

    @Transactional
    public void eliminarRegla(Long id) {
        log.info("Eliminando regla de prioridad con ID: {}", id);
        if (!reglaRepository.existsById(id)) {
            throw new EntityNotFoundException("No se puede eliminar: Regla no encontrada con id: " + id);
        }
        reglaRepository.deleteById(id);
    }
}
