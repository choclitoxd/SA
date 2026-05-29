package com.universidad.pisc.solicitudes.repository;

import com.universidad.pisc.solicitudes.model.SugerenciaIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugerenciaIARepository extends JpaRepository<SugerenciaIA, Long> {
}
