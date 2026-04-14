package com.universidad.pisc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Endpoints de Gestión de Usuarios (Solo ADMIN)
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                
                // Endpoints de Catálogo (ADMIN y COORDINADOR)
                .requestMatchers("/tipos-solicitud/**").hasAnyRole("ADMIN", "COORDINADOR")
                
                // Endpoints de Solicitudes (Acceso según acción)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/solicitudes").hasAnyRole("ESTUDIANTE", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/solicitudes/**").authenticated()
                .requestMatchers("/solicitudes/{id}/clasificar").hasAnyRole("COORDINADOR", "ADMIN")
                .requestMatchers("/solicitudes/{id}/asignar").hasAnyRole("COORDINADOR", "ADMIN")
                .requestMatchers("/solicitudes/{id}/atender").hasAnyRole("ADMIN", "DOCENTE") // DOCENTE puede ser el responsable
                .requestMatchers("/solicitudes/{id}/cerrar").hasAnyRole("ESTUDIANTE", "ADMIN")
                
                // Permitir acceso a H2 Console y Swagger si es necesario para desarrollo
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Para H2 Console
            .httpBasic(org.springframework.security.config.Customizer.withDefaults()); // Autenticación básica para pruebas
        return http.build();
    }
}
