package com.universidad.pisc.catalogo.service;

import com.universidad.pisc.catalogo.dto.ReglaPrioridadMapper;
import com.universidad.pisc.catalogo.dto.ReglaPrioridadRequest;
import com.universidad.pisc.catalogo.dto.ReglaPrioridadResponse;
import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import com.universidad.pisc.catalogo.repository.ReglaPrioridadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de la lógica de negocio de las Reglas de Prioridad.
 * Cumple con SRP al separar la gestión de reglas de la capa de transporte (Controller).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReglaPrioridadService {

    private final ReglaPrioridadRepository reglaRepository;
    private final ReglaPrioridadMapper mapper;

    @Transactional
    public ReglaPrioridadResponse crearRegla(ReglaPrioridadRequest request) {
        log.info("Creando nueva regla de prioridad: {}", request.nombre());
        ReglaPrioridad nuevaRegla = mapper.toEntity(request);
        return mapper.toResponse(reglaRepository.save(nuevaRegla));
    }

    @Transactional
    public ReglaPrioridadResponse actualizarRegla(Long id, ReglaPrioridadRequest datosNuevos) {
        log.info("Actualizando regla de prioridad con ID: {}", id);
        ReglaPrioridad reglaExistente = reglaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Regla de prioridad no encontrada con id: " + id));

        // Lógica de actualización parcial
        if (datosNuevos.nombre() != null) reglaExistente.setNombre(datosNuevos.nombre());
        if (datosNuevos.descripcion() != null) reglaExistente.setDescripcion(datosNuevos.descripcion());
        if (datosNuevos.condicion() != null) reglaExistente.setCondicion(datosNuevos.condicion());
        if (datosNuevos.peso() != null) reglaExistente.setPeso(datosNuevos.peso());
        if (datosNuevos.nivelResultante() != null) reglaExistente.setNivelResultante(datosNuevos.nivelResultante());
        if (datosNuevos.activa() != null) reglaExistente.setActiva(datosNuevos.activa());
        
        return mapper.toResponse(reglaRepository.save(reglaExistente));
    }

    @Transactional(readOnly = true)
    public List<ReglaPrioridadResponse> listarReglasActivas() {
        return reglaRepository.findByActivaTrueOrderByPesoDesc().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
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
