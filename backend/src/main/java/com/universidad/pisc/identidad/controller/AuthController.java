package com.universidad.pisc.identidad.controller;

import com.universidad.pisc.identidad.dto.UsuarioResponse;
import com.universidad.pisc.identidad.dto.UsuarioMapper;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper mapper;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(mapper.toResponse(u)))
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + email));
    }
}
