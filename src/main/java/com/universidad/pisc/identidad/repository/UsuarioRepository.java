package com.universidad.pisc.identidad.repository;

import com.universidad.pisc.identidad.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByIdentificacion(String identificacion);
}
