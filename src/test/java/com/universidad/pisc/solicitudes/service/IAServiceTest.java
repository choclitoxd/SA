package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.model.SugerenciaIA;
import com.universidad.pisc.solicitudes.repository.SugerenciaIARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IAServiceTest {

    @Mock
    private TipoSolicitudRepository tipoRepository;

    @Mock
    private SugerenciaIARepository sugerenciaRepository;

    @Mock
    private TriageService triageService;

    @InjectMocks
    private IAService iaService;

    private SolicitudAcademica solicitud;
    private TipoSolicitud tipo;

    @BeforeEach
    void setUp() {
        // Configurar una solicitud de ejemplo
        solicitud = new SolicitudAcademica();
        solicitud.setCodigo("SOL-001");
        solicitud.setDescripcion("Necesito cancelar una materia por motivos de salud.");

        // Configurar un tipo de solicitud de ejemplo
        tipo = new TipoSolicitud();
        tipo.setId(1L);
        tipo.setNombre("Cancelación de Materia");
        tipo.setDescripcion("Proceso para dar de baja una asignatura.");
        tipo.setActivo(true);

        // Inyectar manualmente la API Key como "NO_KEY" para forzar el modo simulado en el test
        ReflectionTestUtils.setField(iaService, "apiKey", "NO_KEY");
        iaService.init();
    }

    @Test
    void testGenerarSugerenciaSimulada() {
        // GIVEN
        when(tipoRepository.findByActivo(true)).thenReturn(List.of(tipo));
        when(triageService.evaluarPrioridad(any())).thenReturn(NivelPrioridad.MEDIA);
        when(sugerenciaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        SugerenciaIA resultado = iaService.generarSugerencia(solicitud);

        // THEN
        assertNotNull(resultado);
        assertEquals(tipo, resultado.getTipoSugerido());
        assertEquals(NivelPrioridad.MEDIA, resultado.getPrioridadSugerida());
        assertTrue(resultado.getJustificacionIA().contains("Simulación"));
        verify(sugerenciaRepository, times(1)).save(any());
    }

    @Test
    void testGenerarSugerenciaSinTiposActivos() {
        // GIVEN
        when(tipoRepository.findByActivo(true)).thenReturn(List.of());

        // WHEN
        SugerenciaIA resultado = iaService.generarSugerencia(solicitud);

        // THEN
        assertNull(resultado);
        verify(sugerenciaRepository, never()).save(any());
    }
}
