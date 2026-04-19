package com.universidad.pisc.catalogo.controller;

import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import com.universidad.pisc.catalogo.repository.ReglaPrioridadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reglas-prioridad")
@RequiredArgsConstructor
public class ReglaPrioridadController {

    private final ReglaPrioridadRepository reglaRepository;

    @PostMapping
    public ResponseEntity<ReglaPrioridad> crearRegla(@RequestBody ReglaPrioridad regla) {
        return ResponseEntity.ok(reglaRepository.save(regla));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReglaPrioridad> actualizarRegla(@PathVariable Long id, @RequestBody ReglaPrioridad datosNuevos) {
        return reglaRepository.findById(id)
                .map(reglaExistente -> {
                    // Solo actualizamos si el campo viene en el JSON (no es nulo)
                    if (datosNuevos.getNombre() != null) reglaExistente.setNombre(datosNuevos.getNombre());
                    if (datosNuevos.getDescripcion() != null) reglaExistente.setDescripcion(datosNuevos.getDescripcion());
                    if (datosNuevos.getCondicion() != null) reglaExistente.setCondicion(datosNuevos.getCondicion());
                    if (datosNuevos.getPeso() != null) reglaExistente.setPeso(datosNuevos.getPeso());
                    if (datosNuevos.getNivelResultante() != null) reglaExistente.setNivelResultante(datosNuevos.getNivelResultante());
                    if (datosNuevos.getActiva() != null) reglaExistente.setActiva(datosNuevos.getActiva());
                    
                    return ResponseEntity.ok(reglaRepository.save(reglaExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ReglaPrioridad>> listarReglas() {
        return ResponseEntity.ok(reglaRepository.findByActivaTrueOrderByPesoDesc());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegla(@PathVariable Long id) {
        reglaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
