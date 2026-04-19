package com.universidad.pisc.catalogo.dto;

import com.universidad.pisc.catalogo.model.TipoSolicitud;
import org.springframework.stereotype.Component;

@Component
public class TipoSolicitudMapper {

    public TipoSolicitudResponse toResponse(TipoSolicitud tipoSolicitud) {
        if (tipoSolicitud == null) {
            return null;
        }
        return new TipoSolicitudResponse(
                tipoSolicitud.getId(),
                tipoSolicitud.getNombre(),
                tipoSolicitud.getDescripcion(),
                tipoSolicitud.getTiempoAtencionDias(),
                tipoSolicitud.getActivo(),
                tipoSolicitud.getCategoria()
        );
    }

    public TipoSolicitud toEntity(CrearTipoSolicitudRequest request) {
        if (request == null) {
            return null;
        }
        TipoSolicitud tipoSolicitud = new TipoSolicitud();
        tipoSolicitud.setNombre(request.nombre());
        tipoSolicitud.setDescripcion(request.descripcion());
        tipoSolicitud.setTiempoAtencionDias(request.tiempoAtencionDias());
        tipoSolicitud.setCategoria(request.categoria());
        return tipoSolicitud;
    }
}
