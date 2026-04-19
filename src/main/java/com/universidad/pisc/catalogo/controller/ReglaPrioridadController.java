package com.universidad.pisc.catalogo.controller;

import com.universidad.pisc.catalogo.model.ReglaPrioridad;
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
    public ResponseEntity<ReglaPrioridad> crearRegla(@Valid @RequestBody ReglaPrioridad regla) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reglaService.crearRegla(regla));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReglaPrioridad> actualizarRegla(@PathVariable Long id, @RequestBody ReglaPrioridad datosNuevos) {
        return ResponseEntity.ok(reglaService.actualizarRegla(id, datosNuevos));
    }

    @GetMapping
    public ResponseEntity<List<ReglaPrioridad>> listarReglas() {
        return ResponseEntity.ok(reglaService.listarReglasActivas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegla(@PathVariable Long id) {
        reglaService.eliminarRegla(id);
        return ResponseEntity.noContent().build();
    }
}
