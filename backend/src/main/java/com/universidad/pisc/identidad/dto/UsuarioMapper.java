package com.universidad.pisc.identidad.dto;

import com.universidad.pisc.identidad.enums.NombreRol;
import com.universidad.pisc.identidad.model.Rol;
import com.universidad.pisc.identidad.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        Set<NombreRol> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getIdentificacion(),
                roles,
                usuario.getActivo(),
                usuario.getCreadoEn()
        );
    }

    public UsuarioResumen toResumen(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResumen(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getIdentificacion(),
                usuario.getActivo()
        );
    }

    public Usuario toEntity(CrearUsuarioRequest request) {
        if (request == null) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        usuario.setIdentificacion(request.identificacion());
        // La contraseña y la asignación de roles se manejarán en la capa de servicio
        // para incluir lógica adicional como el cifrado de la contraseña.
        return usuario;
    }
}
