package com.universidad.pisc.catalogo.dto;

import com.universidad.pisc.catalogo.enums.CategoriaSolicitud;

public record TipoSolicitudResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer tiempoAtencionDias,
    boolean activo,
    CategoriaSolicitud categoria
) {}
