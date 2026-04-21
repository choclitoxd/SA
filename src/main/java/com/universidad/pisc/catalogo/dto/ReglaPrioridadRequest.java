package com.universidad.pisc.catalogo.dto;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar Reglas de Prioridad.
 * Se usa para el patrón ensamblador en la entrada.
 */
public record ReglaPrioridadRequest(
    @Size(min = 3, max = 100)
    String nombre,
    
    String descripcion,
    
    String condicion,
    
    NivelPrioridad nivelResultante,
    
    @Min(1)
    Integer peso,
    
    Boolean activa
) {}
