package com.universidad.pisc.solicitudes.controller;

import com.universidad.pisc.solicitudes.dto.*;
import com.universidad.pisc.solicitudes.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudDetalleResponse> registrarSolicitud(@Valid @RequestBody RegistrarSolicitudRequest request) {
        SolicitudDetalleResponse respuesta = solicitudService.registrarSolicitud(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(respuesta.id())
                .toUri();
        return ResponseEntity.created(location).body(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudDetalleResponse> obtenerSolicitud(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerSolicitud(id));
    }

    @GetMapping
    public ResponseEntity<Page<SolicitudResumen>> listarSolicitudes(Pageable pageable) {
        return ResponseEntity.ok(solicitudService.listarSolicitudes(pageable));
    }

    @PostMapping("/{id}/clasificar")
    public ResponseEntity<SolicitudDetalleResponse> clasificarSolicitud(
            @PathVariable Long id, 
            @Valid @RequestBody ClasificarSolicitudRequest request) {
        return ResponseEntity.ok(solicitudService.clasificarSolicitud(id, request));
    }

    @PostMapping("/{id}/asignar")
    public ResponseEntity<SolicitudDetalleResponse> asignarResponsable(
            @PathVariable Long id, 
            @Valid @RequestBody AsignarResponsableRequest request) {
        return ResponseEntity.ok(solicitudService.asignarResponsable(id, request));
    }

    @PostMapping("/{id}/atender")
    public ResponseEntity<SolicitudDetalleResponse> marcarAtendida(
            @PathVariable Long id, 
            @Valid @RequestBody MarcarAtendidaRequest request) {
        return ResponseEntity.ok(solicitudService.marcarAtendida(id, request));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudDetalleResponse> cerrarSolicitud(
            @PathVariable Long id, 
            @Valid @RequestBody CerrarSolicitudRequest request) {
        return ResponseEntity.ok(solicitudService.cerrarSolicitud(id, request));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<SolicitudDetalleResponse> rechazarSolicitud(
            @PathVariable Long id, 
            @Valid @RequestBody RechazarSolicitudRequest request) {
        return ResponseEntity.ok(solicitudService.rechazarSolicitud(id, request));
    }
}
