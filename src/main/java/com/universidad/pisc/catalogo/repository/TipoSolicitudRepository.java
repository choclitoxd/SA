package com.universidad.pisc.catalogo.repository;

import com.universidad.pisc.catalogo.model.TipoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TipoSolicitudRepository extends JpaRepository<TipoSolicitud, Long> {
    List<TipoSolicitud> findByActivo(boolean activo);
    Optional<TipoSolicitud> findByNombre(String nombre);
}
