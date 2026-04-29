package com.universidad.pisc.solicitudes.repository;

import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAcademicaRepository extends JpaRepository<SolicitudAcademica, Long> {
    
    /**
     * Recupera solicitudes filtradas por el email del solicitante (para cumplimiento de BOLA).
     */
    Page<SolicitudAcademica> findBySolicitanteEmail(String email, Pageable pageable);
}
