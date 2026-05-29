package com.universidad.pisc.solicitudes.repository;

import com.universidad.pisc.solicitudes.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
}
