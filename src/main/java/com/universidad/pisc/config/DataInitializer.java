package com.universidad.pisc.config;

import com.universidad.pisc.identidad.model.NombreRol;
import com.universidad.pisc.identidad.model.Rol;
import com.universidad.pisc.identidad.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) {
        log.info("Verificando existencia de roles iniciales...");
        
        Arrays.stream(NombreRol.values()).forEach(nombre -> {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre(nombre);
                nuevoRol.setDescripcion("Rol de " + nombre.name());
                rolRepository.save(nuevoRol);
                log.info("Rol {} creado exitosamente.", nombre);
            }
        });
    }
}
