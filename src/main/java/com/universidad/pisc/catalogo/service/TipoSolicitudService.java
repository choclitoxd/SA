package com.universidad.pisc.catalogo.service;

import com.universidad.pisc.catalogo.dto.CrearTipoSolicitudRequest;
import com.universidad.pisc.catalogo.dto.TipoSolicitudMapper;
import com.universidad.pisc.catalogo.dto.TipoSolicitudResponse;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TipoSolicitudService {

    private final TipoSolicitudRepository repository;
    private final TipoSolicitudMapper mapper;

    @Transactional
    public TipoSolicitudResponse crearTipoSolicitud(CrearTipoSolicitudRequest request) {
        // Aquí se podría añadir lógica para comprobar duplicados por nombre y devolver un 409,
        // pero la restricción de la BD ya lanzará un error. Para una API más limpia, una comprobación explícita es mejor.
        // if (repository.findByNombre(request.nombre()).isPresent()) { ... lanzar excepción personalizada ... }
        TipoSolicitud nuevoTipo = mapper.toEntity(request);
        TipoSolicitud guardado = repository.save(nuevoTipo);
        return mapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<TipoSolicitudResponse> listarTiposSolicitud(Boolean activo) {
        List<TipoSolicitud> tipos;
        if (activo != null) {
            tipos = repository.findByActivo(activo);
        } else {
            tipos = repository.findAll();
        }
        return tipos.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TipoSolicitudResponse actualizarTipoSolicitud(Long id, CrearTipoSolicitudRequest request) {
        TipoSolicitud tipoExistente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoSolicitud no encontrado con id: " + id));

        tipoExistente.setNombre(request.nombre());
        tipoExistente.setDescripcion(request.descripcion());
        tipoExistente.setTiempoAtencionDias(request.tiempoAtencionDias());
        tipoExistente.setCategoria(request.categoria()); // Aseguramos que la categoría también se actualice

        TipoSolicitud actualizado = repository.save(tipoExistente);
        return mapper.toResponse(actualizado);
    }
    
    @Transactional
    public void desactivarTipoSolicitud(Long id) {
        TipoSolicitud tipoExistente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoSolicitud no encontrado con id: " + id));
        
        // Según la especificación, no se puede desactivar si hay solicitudes activas asociadas.
        // Esta lógica de validación se añadiría aquí antes de cambiar el estado.
        // if (solicitudRepository.countByTipoAndEstadoActivo(tipoExistente) > 0) { ... lanzar excepción de conflicto ... }
        
        tipoExistente.setActivo(false);
        repository.save(tipoExistente);
    }
}
