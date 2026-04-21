package com.universidad.pisc.config;

import com.universidad.pisc.catalogo.model.Categoria;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.CategoriaRepository;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
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

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoSolicitudRepository tipoSolicitudRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando inicialización de datos de semilla...");
        crearRoles();
        crearAdminInicial();
        Map<String, Categoria> categorias = crearCategorias();
        crearTiposSolicitudIniciales(categorias);
        log.info("Inicialización de datos completada.");
    }

    private Map<String, Categoria> crearCategorias() {
        Map<String, Categoria> mapa = new HashMap<>();
        String[][] categoriasData = {
            {"GESTION_CURRICULAR", "Trámites relacionados con el plan de estudios y asignaturas."},
            {"PERMANENCIA_ACADEMICA", "Solicitudes de reingreso, traslados y continuidad."},
            {"DOCUMENTACION", "Certificados, diplomas y actas."},
            {"GRADOS_Y_EGRESADOS", "Trámites de finalización de estudios y relación con egresados."},
            {"BIENESTAR_Y_APOYO", "Servicios de salud, psicología y apoyo económico."},
            {"OTROS", "Otras solicitudes no clasificadas."}
        };

        for (String[] data : categoriasData) {
            Categoria cat = categoriaRepository.findByNombre(data[0])
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria(data[0], data[1]);
                        return categoriaRepository.save(nueva);
                    });
            mapa.put(data[0], cat);
        }
        return mapa;
    }

    private void crearTiposSolicitudIniciales(Map<String, Categoria> categorias) {
        if (tipoSolicitudRepository.count() == 0) {
            log.info("Poblando catálogo inicial de tipos de solicitud...");
            
            saveTipo("RECLAMO_CALIFICACION", "Reclamación sobre notas o exámenes parciales y finales.", categorias.get("GESTION_CURRICULAR"));
            saveTipo("SOLICITUD_CERTIFICADO", "Expedición de certificados de estudio o notas.", categorias.get("DOCUMENTACION"));
            saveTipo("CANCELACION_SEMESTRE", "Retiro formal de todas las asignaturas inscritas.", categorias.get("PERMANENCIA_ACADEMICA"));
            saveTipo("BECA_SOCIOCONOMICA", "Postulación para apoyos financieros de bienestar.", categorias.get("BIENESTAR_Y_APOYO"));
            saveTipo("SOLICITUD_GRADO", "Proceso de postulación y revisión de requisitos para grado.", categorias.get("GRADOS_Y_EGRESADOS"));

            log.info("Catálogo poblado exitosamente con 5 tipos básicos.");
        }
    }

    private void saveTipo(String nombre, String descripcion, Categoria cat) {
        if (tipoSolicitudRepository.findByNombre(nombre).isEmpty()) {
            TipoSolicitud tipo = new TipoSolicitud();
            tipo.setNombre(nombre);
            tipo.setDescripcion(descripcion);
            tipo.setCategoria(cat);
            tipo.setTiempoAtencionDias(5);
            tipo.setActivo(true);
            tipoSolicitudRepository.save(tipo);
        }
    }

    private void crearRoles() {
        Arrays.stream(NombreRol.values()).forEach(nombre -> {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                Rol nuevoRol = new Rol();
                nuevoRol.setNombre(nombre);
                nuevoRol.setDescripcion("Rol de " + nombre.name());
                rolRepository.save(nuevoRol);
            }
        });
    }

    private void crearAdminInicial() {
        String adminEmail = "admin@pisc.edu.co";
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
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
            log.info("Usuario administrador creado: {}", adminEmail);
        }
    }
}
