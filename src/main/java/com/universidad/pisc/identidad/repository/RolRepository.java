package com.universidad.pisc.identidad.repository;

import com.universidad.pisc.identidad.model.Rol;
import com.universidad.pisc.identidad.model.NombreRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(NombreRol nombre);
}
