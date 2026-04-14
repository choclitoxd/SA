package com.universidad.pisc.catalogo.repository;

import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReglaPrioridadRepository extends JpaRepository<ReglaPrioridad, Long> {
    List<ReglaPrioridad> findByActivaTrueOrderByPesoDesc();
}
