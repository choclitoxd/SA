package com.universidad.pisc.catalogo.controller;

import com.universidad.pisc.catalogo.dto.ReglaPrioridadRequest;
import com.universidad.pisc.catalogo.dto.ReglaPrioridadResponse;
import com.universidad.pisc.catalogo.service.ReglaPrioridadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reglas-prioridad")
@RequiredArgsConstructor
public class ReglaPrioridadController {

    private final ReglaPrioridadService reglaService;

    @PostMapping
    public ResponseEntity<ReglaPrioridadResponse> crearRegla(@Valid @RequestBody ReglaPrioridadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reglaService.crearRegla(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReglaPrioridadResponse> actualizarRegla(@PathVariable Long id, @RequestBody ReglaPrioridadRequest datosNuevos) {
        return ResponseEntity.ok(reglaService.actualizarRegla(id, datosNuevos));
    }

    @GetMapping
    public ResponseEntity<List<ReglaPrioridadResponse>> listarReglas() {
        return ResponseEntity.ok(reglaService.listarReglasActivas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegla(@PathVariable Long id) {
        reglaService.eliminarRegla(id);
        return ResponseEntity.noContent().build();
    }
}
