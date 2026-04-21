package com.universidad.pisc.catalogo.service;

import com.universidad.pisc.catalogo.dto.ActualizarTipoSolicitudRequest;
import com.universidad.pisc.catalogo.dto.CrearTipoSolicitudRequest;
import com.universidad.pisc.catalogo.dto.TipoSolicitudMapper;
import com.universidad.pisc.catalogo.dto.TipoSolicitudResponse;
import com.universidad.pisc.catalogo.model.Categoria;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.CategoriaRepository;
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
    private final CategoriaRepository categoriaRepository;
    private final TipoSolicitudMapper mapper;

    @Transactional
    public TipoSolicitudResponse crearTipoSolicitud(CrearTipoSolicitudRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + request.categoriaId()));

        TipoSolicitud nuevoTipo = mapper.toEntity(request);
        nuevoTipo.setCategoria(categoria);
        
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
    public TipoSolicitudResponse actualizarTipoSolicitud(Long id, ActualizarTipoSolicitudRequest request) {
        TipoSolicitud tipoExistente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TipoSolicitud no encontrado con id: " + id));

        if (request.nombre() != null) {
            tipoExistente.setNombre(request.nombre());
        }
        if (request.descripcion() != null) {
            tipoExistente.setDescripcion(request.descripcion());
        }
        if (request.tiempoAtencionDias() != null) {
            tipoExistente.setTiempoAtencionDias(request.tiempoAtencionDias());
        }
        if (request.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + request.categoriaId()));
            tipoExistente.setCategoria(categoria);
        }
        if (request.activo() != null) {
            tipoExistente.setActivo(request.activo());
        }

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
