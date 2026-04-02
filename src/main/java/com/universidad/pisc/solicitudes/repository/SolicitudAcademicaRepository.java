package com.universidad.pisc.solicitudes.repository;

import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAcademicaRepository extends JpaRepository<SolicitudAcademica, Long> {
    // Aquí se podrían añadir métodos para consultas personalizadas en el futuro,
    // por ejemplo, para buscar solicitudes por estado, solicitante, etc.
}
