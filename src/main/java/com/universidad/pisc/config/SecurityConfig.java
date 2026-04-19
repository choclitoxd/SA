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
                // Endpoints de Gestión de Usuarios (Solo ADMINISTRATIVO)
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRATIVO")
                
                // Endpoints de Catálogo y Reglas (ADMINISTRATIVO y COORDINADOR)
                .requestMatchers("/tipos-solicitud/**").hasAnyRole("ADMINISTRATIVO", "COORDINADOR")
                .requestMatchers("/reglas-prioridad/**").hasAnyRole("ADMINISTRATIVO", "COORDINADOR")
                
                // Endpoints de Solicitudes (Acceso según acción)
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/solicitudes").hasAnyRole("ESTUDIANTE", "ADMINISTRATIVO")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/solicitudes/**").authenticated()
                .requestMatchers("/solicitudes/{id}/clasificar").hasAnyRole("COORDINADOR", "ADMINISTRATIVO")
                .requestMatchers("/solicitudes/{id}/asignar").hasAnyRole("COORDINADOR", "ADMINISTRATIVO")
                .requestMatchers("/solicitudes/{id}/atender").hasAnyRole("ADMINISTRATIVO", "DIRECTOR", "COORDINADOR")
                .requestMatchers("/solicitudes/{id}/cerrar").hasAnyRole("ESTUDIANTE", "ADMINISTRATIVO")
                
                // Permitir acceso a H2 Console y Swagger si es necesario para desarrollo
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
