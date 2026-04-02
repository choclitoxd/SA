package com.universidad.pisc.catalogo.controller;

import com.universidad.pisc.catalogo.dto.CrearTipoSolicitudRequest;
import com.universidad.pisc.catalogo.dto.TipoSolicitudResponse;
import com.universidad.pisc.catalogo.service.TipoSolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tipos-solicitud") // Ruta base del OpenAPI
@RequiredArgsConstructor
public class TipoSolicitudController {

    private final TipoSolicitudService tipoSolicitudService;

    @GetMapping
    public ResponseEntity<List<TipoSolicitudResponse>> listarTiposSolicitud(
            @RequestParam(required = false, defaultValue = "true") Boolean activo) {
        List<TipoSolicitudResponse> respuesta = tipoSolicitudService.listarTiposSolicitud(activo);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    public ResponseEntity<TipoSolicitudResponse> crearTipoSolicitud(@Valid @RequestBody CrearTipoSolicitudRequest request) {
        TipoSolicitudResponse respuesta = tipoSolicitudService.crearTipoSolicitud(request);
        
        // Construye la URL del nuevo recurso creado, como es estándar en APIs REST.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(respuesta.id())
                .toUri();

        // Devuelve 201 Created con la ubicación y el cuerpo del nuevo recurso.
        return ResponseEntity.created(location).body(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoSolicitudResponse> actualizarTipoSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody CrearTipoSolicitudRequest request) {
        TipoSolicitudResponse respuesta = tipoSolicitudService.actualizarTipoSolicitud(id, request);
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarTipoSolicitud(@PathVariable Long id) {
        tipoSolicitudService.desactivarTipoSolicitud(id);
        // Devuelve 204 No Content, indicando éxito sin cuerpo en la respuesta.
        return ResponseEntity.noContent().build();
    }
}
