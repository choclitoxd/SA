package com.universidad.pisc.catalogo.dto;

public record TipoSolicitudResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer tiempoAtencionDias,
    Boolean activo,
    String categoriaNombre,
    Long categoriaId
) {}
