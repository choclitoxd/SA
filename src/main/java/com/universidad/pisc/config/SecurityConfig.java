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
                // Endpoints de Gestión de Usuarios (ADMINISTRATIVO o DIRECTOR)
                .requestMatchers("/usuarios/**").hasAnyRole("ADMINISTRATIVO", "DIRECTOR")
                
                // Endpoints de Catálogo y Reglas (ADMINISTRATIVO, COORDINADOR o DIRECTOR)
                .requestMatchers("/tipos-solicitud/**").hasAnyRole("ADMINISTRATIVO", "COORDINADOR", "DIRECTOR")
                .requestMatchers("/reglas-prioridad/**").hasAnyRole("ADMINISTRATIVO", "COORDINADOR", "DIRECTOR")
                
                // Endpoints de Solicitudes (Acceso según acción)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/solicitudes").hasAnyRole("ESTUDIANTE", "DOCENTE", "ADMINISTRATIVO")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/solicitudes/**").authenticated()
                .requestMatchers("/solicitudes/{id}/clasificar").hasAnyRole("COORDINADOR", "ADMINISTRATIVO", "DIRECTOR")
                .requestMatchers("/solicitudes/{id}/asignar").hasAnyRole("COORDINADOR", "ADMINISTRATIVO", "DIRECTOR")
                .requestMatchers("/solicitudes/{id}/atender").hasAnyRole("DOCENTE", "ADMINISTRATIVO", "DIRECTOR", "COORDINADOR")
                .requestMatchers("/solicitudes/{id}/cerrar").hasAnyRole("ESTUDIANTE", "DOCENTE", "ADMINISTRATIVO", "DIRECTOR", "COORDINADOR")

                
                // Permitir acceso a H2 Console y Swagger si es necesario para desarrollo
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
