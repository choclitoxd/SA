package com.universidad.pisc.identidad.controller;

import com.universidad.pisc.identidad.dto.ActualizarUsuarioRequest;
import com.universidad.pisc.identidad.dto.CrearUsuarioRequest;
import com.universidad.pisc.identidad.dto.UsuarioResponse;
import com.universidad.pisc.identidad.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioResponse respuesta = usuarioService.crearUsuario(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(respuesta.id())
                .toUri();
        return ResponseEntity.created(location).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listarUsuarios(Pageable pageable) {
        // NOTA: La especificación permite filtrar por 'activo' y 'rol'.
        // Esto requeriría mejorar el servicio y el repositorio.
        // Por ahora, devolvemos una lista paginada.
        Page<UsuarioResponse> respuesta = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioResponse respuesta = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request) {
        UsuarioResponse respuesta = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(respuesta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
