package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import com.universidad.pisc.solicitudes.dto.*;
import com.universidad.pisc.solicitudes.enums.CanalOrigen;
import com.universidad.pisc.solicitudes.enums.EstadoSolicitud;
import com.universidad.pisc.solicitudes.model.*;
import com.universidad.pisc.solicitudes.repository.AsignacionRepository;
import com.universidad.pisc.solicitudes.repository.SolicitudAcademicaRepository;
import com.universidad.pisc.solicitudes.repository.SugerenciaIARepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudAcademicaRepository solicitudRepository;
    @Mock
    private SolicitudHistorialService historialService;
    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TipoSolicitudRepository tipoSolicitudRepository;
    @Mock
    private TriageService triageService;
    @Mock
    private SolicitudClasificador clasificadorIA;
    @Mock
    private SugerenciaIARepository sugerenciaIARepository;
    @Mock
    private SolicitudMapper mapper;

    @InjectMocks
    private SolicitudService solicitudService;

    private Usuario solicitante;
    private TipoSolicitud tipoSolicitud;
    private SolicitudAcademica solicitud;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario();
        solicitante.setId(1L);
        solicitante.setIdentificacion("12345678");
        solicitante.setEmail("test@universidad.edu.co");
        solicitante.setNombre("Test");
        solicitante.setApellido("User");
        solicitante.setActivo(true);

        tipoSolicitud = new TipoSolicitud();
        tipoSolicitud.setId(1L);
        tipoSolicitud.setNombre("Homologación");

        solicitud = new SolicitudAcademica();
        solicitud.setId(10L);
        solicitud.setCodigo("SOL-2026-001");
        solicitud.setEstado(EstadoSolicitud.REGISTRADA);
        solicitud.setSolicitante(solicitante);
        solicitud.setVersion(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email, String role) {
        Authentication authentication = mock(Authentication.class, withSettings().lenient());
        lenient().when(authentication.getName()).thenReturn(email);
        lenient().doReturn(Collections.singletonList(new SimpleGrantedAuthority(role)))
            .when(authentication).getAuthorities();
        
        SecurityContext securityContext = mock(SecurityContext.class, withSettings().lenient());
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registrarSolicitud_Success() {
        // Arrange
        RegistrarSolicitudRequest request = new RegistrarSolicitudRequest(
                "Descripción de prueba mayor a 30 caracteres",
                CanalOrigen.CORREO,
                "12345678",
                null
        );

        when(usuarioRepository.findByIdentificacion(anyString())).thenReturn(Optional.of(solicitante));
        when(solicitudRepository.save(any(SolicitudAcademica.class))).thenReturn(solicitud);
        when(mapper.toDetalleResponse(any(SolicitudAcademica.class))).thenReturn(mock(SolicitudDetalleResponse.class));

        // Act
        SolicitudDetalleResponse response = solicitudService.registrarSolicitud(request);

        // Assert
        assertNotNull(response);
        verify(solicitudRepository, times(1)).save(any(SolicitudAcademica.class));
        verify(clasificadorIA, times(1)).generarSugerencia(any());
        verify(historialService, times(1)).registrarEvento(any(), anyString(), any(), any(), anyString());
    }

    @Test
    void clasificarSolicitud_Manual_Success() {
        // Arrange
        mockSecurityContext("admin@universidad.edu.co", "ROLE_ADMINISTRATIVO");
        ClasificarSolicitudRequest request = new ClasificarSolicitudRequest(
                1L, NivelPrioridad.ALTA, "Justificación manual", null, 1L
        );

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(tipoSolicitudRepository.findById(1L)).thenReturn(Optional.of(tipoSolicitud));
        when(solicitudRepository.save(any(SolicitudAcademica.class))).thenReturn(solicitud);

        // Act
        solicitudService.clasificarSolicitud(10L, request);

        // Assert
        assertEquals(EstadoSolicitud.CLASIFICADA, solicitud.getEstado());
        assertEquals(NivelPrioridad.ALTA, solicitud.getPrioridad().getNivel());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void asignarResponsable_Success() {
        // Arrange
        mockSecurityContext("admin@universidad.edu.co", "ROLE_ADMINISTRATIVO");
        solicitud.setEstado(EstadoSolicitud.CLASIFICADA);
        AsignarResponsableRequest request = new AsignarResponsableRequest(2L, "Nota de asignación", 1L);
        Usuario responsable = new Usuario();
        responsable.setId(2L);
        responsable.setActivo(true);
        responsable.setNombre("Responsable");
        responsable.setApellido("Test");

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(responsable));
        when(solicitudRepository.save(any(SolicitudAcademica.class))).thenReturn(solicitud);
        when(historialService.obtenerUsuarioActualParaAsignacion(any())).thenReturn(solicitante);

        // Act
        solicitudService.asignarResponsable(10L, request);

        // Assert
        assertEquals(EstadoSolicitud.EN_ATENCION, solicitud.getEstado());
        assertFalse(solicitud.getAsignaciones().isEmpty());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void obtenerSolicitud_DenegadoSiEsOtroEstudiante() {
        // Arrange
        mockSecurityContext("otro@universidad.edu.co", "ROLE_ESTUDIANTE");
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));

        // Act & Assert
        assertThrows(com.universidad.pisc.config.BusinessException.class, () -> {
            solicitudService.obtenerSolicitud(10L);
        });
    }

    @Test
    void obtenerSolicitud_PermitidoSiEsElMismoEstudiante() {
        // Arrange
        mockSecurityContext("test@universidad.edu.co", "ROLE_ESTUDIANTE");
        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(mapper.toDetalleResponse(any())).thenReturn(mock(SolicitudDetalleResponse.class));

        // Act
        SolicitudDetalleResponse response = solicitudService.obtenerSolicitud(10L);

        // Assert
        assertNotNull(response);
    }
}
