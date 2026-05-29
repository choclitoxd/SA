package com.universidad.pisc.identidad.service;

import com.universidad.pisc.identidad.dto.ActualizarUsuarioRequest;
import com.universidad.pisc.identidad.dto.CrearUsuarioRequest;
import com.universidad.pisc.identidad.dto.UsuarioMapper;
import com.universidad.pisc.identidad.dto.UsuarioResponse;
import com.universidad.pisc.identidad.model.Rol;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.RolRepository;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest request) {
        // Validar duplicados de Email
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Ya existe un usuario registrado con el email: " + request.email());
        }
        
        // Validar duplicados de Identificación
        if (usuarioRepository.findByIdentificacion(request.identificacion()).isPresent()) {
            throw new IllegalStateException("Ya existe un usuario registrado con la identificación: " + request.identificacion());
        }

        Usuario usuario = mapper.toEntity(request);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));

        Set<Rol> roles = request.roles().stream()
                .map(nombreRol -> rolRepository.findByNombre(nombreRol)
                        .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + nombreRol)))
                .collect(Collectors.toSet());
        usuario.setRoles(roles);

        Usuario nuevoUsuario = usuarioRepository.save(usuario);
        return mapper.toResponse(nuevoUsuario);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        if (request.nombre() != null) {
            usuario.setNombre(request.nombre());
        }
        if (request.apellido() != null) {
            usuario.setApellido(request.apellido());
        }
        if (request.email() != null && !request.email().equalsIgnoreCase(usuario.getEmail())) {
            if (usuarioRepository.findByEmail(request.email()).isPresent()) {
                throw new IllegalStateException("No se puede actualizar: el email " + request.email() + " ya está en uso por otro usuario.");
            }
            usuario.setEmail(request.email());
        }
        if (request.roles() != null && !request.roles().isEmpty()) {
            Set<Rol> roles = request.roles().stream()
                    .map(nombreRol -> rolRepository.findByNombre(nombreRol)
                            .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + nombreRol)))
                    .collect(Collectors.toSet());
            usuario.setRoles(roles);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapper.toResponse(usuarioActualizado);
    }

    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        // La especificación indica que desactivar un usuario puede disparar otros eventos.
        // Esa lógica de negocio se implementaría aquí.
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }
}
