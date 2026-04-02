package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.model.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.identidad.repository.UsuarioRepository;
import com.universidad.pisc.solicitudes.dto.*;
import com.universidad.pisc.solicitudes.model.*;
import com.universidad.pisc.solicitudes.repository.AsignacionRepository;
import com.universidad.pisc.solicitudes.repository.HistorialSolicitudRepository;
import com.universidad.pisc.solicitudes.repository.SolicitudAcademicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudAcademicaRepository solicitudRepository;
    @Mock
    private HistorialSolicitudRepository historialRepository;
    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TipoSolicitudRepository tipoSolicitudRepository;
    @Mock
    private TriageService triageService;
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
        verify(historialRepository, times(1)).save(any(HistorialSolicitud.class));
    }

    @Test
    void clasificarSolicitud_Manual_Success() {
        // Arrange
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
    void clasificarSolicitud_AutoTriage_Success() {
        // Arrange
        ClasificarSolicitudRequest request = new ClasificarSolicitudRequest(
                1L, null, null, null, 1L
        );

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(tipoSolicitudRepository.findById(1L)).thenReturn(Optional.of(tipoSolicitud));
        when(triageService.evaluarPrioridad(any(SolicitudAcademica.class))).thenReturn(NivelPrioridad.CRITICA);
        when(solicitudRepository.save(any(SolicitudAcademica.class))).thenReturn(solicitud);

        // Act
        solicitudService.clasificarSolicitud(10L, request);

        // Assert
        assertEquals(EstadoSolicitud.CLASIFICADA, solicitud.getEstado());
        assertEquals(NivelPrioridad.CRITICA, solicitud.getPrioridad().getNivel());
        assertTrue(solicitud.getPrioridad().getJustificacion().contains("automáticamente"));
        verify(triageService).evaluarPrioridad(solicitud);
    }

    @Test
    void asignarResponsable_Success() {
        // Arrange
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

        // Act
        solicitudService.asignarResponsable(10L, request);

        // Assert
        assertEquals(EstadoSolicitud.EN_ATENCION, solicitud.getEstado());
        assertFalse(solicitud.getAsignaciones().isEmpty());
        assertEquals(responsable, solicitud.getAsignaciones().get(0).getResponsable());
        verify(solicitudRepository).save(solicitud);
    }
}
