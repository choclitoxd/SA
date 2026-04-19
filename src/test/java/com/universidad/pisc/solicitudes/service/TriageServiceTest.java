package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.ReglaPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.ReglaPrioridadRepository;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TriageServiceTest {

    @Mock
    private ReglaPrioridadRepository reglaRepository;

    @InjectMocks
    private TriageService triageService;

    private SolicitudAcademica solicitud;
    private TipoSolicitud tipoReclamo;

    @BeforeEach
    void setUp() {
        tipoReclamo = new TipoSolicitud();
        tipoReclamo.setNombre("RECLAMO");

        solicitud = new SolicitudAcademica();
        solicitud.setCodigo("SOL-001");
        solicitud.setDescripcion("Mi solicitud es urgente porque necesito graduarme.");
        solicitud.setTipo(tipoReclamo);
    }

    @Test
    void evaluarPrioridad_DeberiaRetornarAlta_CuandoEsReclamoYContieneUrgente() {
        // Regla: Si el tipo es RECLAMO y la descripción contiene 'urgente'
        ReglaPrioridad reglaAlta = new ReglaPrioridad();
        reglaAlta.setNombre("Regla Crítica");
        reglaAlta.setCondicion("tipoNombre == 'RECLAMO' and descripcion.contains('urgente')");
        reglaAlta.setNivelResultante(NivelPrioridad.ALTA);
        reglaAlta.setPeso(100);

        when(reglaRepository.findByActivaTrueOrderByPesoDesc())
                .thenReturn(Collections.singletonList(reglaAlta));

        NivelPrioridad resultado = triageService.evaluarPrioridad(solicitud);

        assertEquals(NivelPrioridad.ALTA, resultado);
    }

    @Test
    void evaluarPrioridad_DeberiaRetornarMedia_CuandoSoloEsReclamo() {
        // Regla 1 (Peso 100): Tipo RECLAMO y urgente -> ALTA (No va a coincidir porque cambiaremos la descripción)
        ReglaPrioridad reglaAlta = new ReglaPrioridad();
        reglaAlta.setCondicion("tipoNombre == 'RECLAMO' and descripcion.contains('urgente')");
        reglaAlta.setNivelResultante(NivelPrioridad.ALTA);
        reglaAlta.setPeso(100);

        // Regla 2 (Peso 50): Solo Tipo RECLAMO -> MEDIA
        ReglaPrioridad reglaMedia = new ReglaPrioridad();
        reglaMedia.setCondicion("tipoNombre == 'RECLAMO'");
        reglaMedia.setNivelResultante(NivelPrioridad.MEDIA);
        reglaMedia.setPeso(50);

        solicitud.setDescripcion("Una consulta normal sobre un reclamo.");

        when(reglaRepository.findByActivaTrueOrderByPesoDesc())
                .thenReturn(Arrays.asList(reglaAlta, reglaMedia));

        NivelPrioridad resultado = triageService.evaluarPrioridad(solicitud);

        assertEquals(NivelPrioridad.MEDIA, resultado);
    }

    @Test
    void evaluarPrioridad_DeberiaRetornarBaja_CuandoNoCoincideNingunaRegla() {
        when(reglaRepository.findByActivaTrueOrderByPesoDesc())
                .thenReturn(Collections.emptyList());

        NivelPrioridad resultado = triageService.evaluarPrioridad(solicitud);

        assertEquals(NivelPrioridad.BAJA, resultado);
    }
}
