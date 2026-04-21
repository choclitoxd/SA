package com.universidad.pisc.catalogo.dto;

import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import org.springframework.stereotype.Component;

/**
 * Ensamblador (Mapper) para convertir entre la entidad ReglaPrioridad y sus DTOs.
 */
@Component
public class ReglaPrioridadMapper {

    public ReglaPrioridad toEntity(ReglaPrioridadRequest request) {
        if (request == null) return null;
        
        ReglaPrioridad entidad = new ReglaPrioridad();
        entidad.setNombre(request.nombre());
        entidad.setDescripcion(request.descripcion());
        entidad.setCondicion(request.condicion());
        entidad.setNivelResultante(request.nivelResultante());
        entidad.setPeso(request.peso());
        if (request.activa() != null) {
            entidad.setActiva(request.activa());
        }
        return entidad;
    }

    public ReglaPrioridadResponse toResponse(ReglaPrioridad entidad) {
        if (entidad == null) return null;
        
        return new ReglaPrioridadResponse(
            entidad.getId(),
            entidad.getNombre(),
            entidad.getDescripcion(),
            entidad.getCondicion(),
            entidad.getNivelResultante(),
            entidad.getPeso(),
            entidad.getActiva(),
            entidad.getVersion()
        );
    }
}
