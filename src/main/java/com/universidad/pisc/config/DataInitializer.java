package com.universidad.pisc.config;

import com.universidad.pisc.identidad.enums.NombreRol;
import com.universidad.pisc.identidad.model.Rol;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.RolRepository;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        crearRoles();
        crearAdminInicial();
    }

    private void crearRoles() {
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

    private void crearAdminInicial() {
        String adminEmail = "admin@pisc.edu.co";
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            log.info("Creando usuario administrador inicial...");
            
            Rol rolAdmin = rolRepository.findByNombre(NombreRol.ADMINISTRATIVO)
                    .orElseThrow(() -> new RuntimeException("Error: Rol ADMINISTRATIVO no encontrado."));

            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setIdentificacion("00000000");
            admin.setActivo(true);
            
            Set<Rol> roles = new HashSet<>();
            roles.add(rolAdmin);
            admin.setRoles(roles);

            usuarioRepository.save(admin);
            log.info("Usuario administrador creado: {}. Usa estas credenciales para empezar en Postman.", adminEmail);
        }
    }
}
