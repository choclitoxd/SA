package com.universidad.pisc.catalogo.service;

import com.universidad.pisc.catalogo.model.Categoria;
import com.universidad.pisc.catalogo.repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository repository;

    @Transactional
    public Categoria crearCategoria(Categoria categoria) {
        if (repository.findByNombre(categoria.getNombre()).isPresent()) {
            throw new IllegalStateException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return repository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias(Boolean soloActivas) {
        if (Boolean.TRUE.equals(soloActivas)) {
            return repository.findByActivaTrue();
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con ID: " + id));
    }
}
