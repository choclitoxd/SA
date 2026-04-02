package com.universidad.pisc.solicitudes.repository;

import com.universidad.pisc.solicitudes.model.HistorialSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialSolicitudRepository extends JpaRepository<HistorialSolicitud, Long> {
}
