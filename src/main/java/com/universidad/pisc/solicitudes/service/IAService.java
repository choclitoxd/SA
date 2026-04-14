package com.universidad.pisc.solicitudes.service;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.catalogo.repository.TipoSolicitudRepository;
import com.universidad.pisc.solicitudes.model.SolicitudAcademica;
import com.universidad.pisc.solicitudes.model.SugerenciaIA;
import com.universidad.pisc.solicitudes.repository.SugerenciaIARepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que integra Google Gemini 1.5 Flash para la clasificación 
 * automática de solicitudes académicas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IAService {

    private final TipoSolicitudRepository tipoRepository;
    private final SugerenciaIARepository sugerenciaRepository;
    private final TriageService triageService;

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    private ChatLanguageModel model;

    @PostConstruct
    public void init() {
        if ("PONER_AQUI_TU_API_KEY".equals(apiKey) || "NO_KEY".equals(apiKey)) {
            log.warn("API Key de Gemini no configurada. Las sugerencias de IA serán simuladas.");
            this.model = null;
        } else {
            this.model = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gemini-1.5-flash")
                    .build();
        }
    }

    /**
     * Analiza una solicitud y genera una sugerencia de clasificación usando IA.
     */
    @Transactional
    public SugerenciaIA generarSugerencia(SolicitudAcademica solicitud) {
        log.info("Generando sugerencia de IA para la solicitud: {}", solicitud.getCodigo());

        List<TipoSolicitud> tiposActivos = tipoRepository.findByActivo(true);
        if (tiposActivos.isEmpty()) {
            return null;
        }

        if (model == null) {
            return generarSugerenciaSimulada(solicitud, tiposActivos);
        }

        try {
            String listaTipos = tiposActivos.stream()
                    .map(t -> "- " + t.getNombre() + ": " + t.getDescripcion())
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(
                    "Eres un experto en gestión académica universitaria. Clasifica la siguiente solicitud de un estudiante.\n\n" +
                    "DESCRIPCIÓN DEL ESTUDIANTE: \"%s\"\n\n" +
                    "TIPOS DE SOLICITUD DISPONIBLES:\n%s\n\n" +
                    "INSTRUCCIÓN: Elige el tipo de solicitud que mejor se ajuste. Responde en este formato exacto:\n" +
                    "TIPO: [Nombre del Tipo]\n" +
                    "JUSTIFICACIÓN: [Breve explicación de por qué]",
                    solicitud.getDescripcion(), listaTipos
            );

            String response = model.generate(prompt);
            return procesarRespuestaIA(response, solicitud, tiposActivos);

        } catch (Exception e) {
            log.error("Error al llamar a Gemini API: {}", e.getMessage());
            return generarSugerenciaSimulada(solicitud, tiposActivos);
        }
    }

    private SugerenciaIA procesarRespuestaIA(String response, SolicitudAcademica solicitud, List<TipoSolicitud> tipos) {
        String tipoNombre = extraerValor(response, "TIPO:");
        String justificacion = extraerValor(response, "JUSTIFICACIÓN:");

        TipoSolicitud sugerido = tipos.stream()
                .filter(t -> t.getNombre().equalsIgnoreCase(tipoNombre))
                .findFirst()
                .orElse(tipos.get(0));

        NivelPrioridad prioridadSugerida = triageService.evaluarPrioridad(solicitud);

        SugerenciaIA sugerencia = new SugerenciaIA();
        sugerencia.setSolicitud(solicitud);
        sugerencia.setTipoSugerido(sugerido);
        sugerencia.setPrioridadSugerida(prioridadSugerida);
        sugerencia.setConfianza(0.92);
        sugerencia.setJustificacionIA(justificacion != null ? justificacion : "Clasificación automática via Gemini 1.5 Flash.");

        return sugerenciaRepository.save(sugerencia);
    }

    private String extraerValor(String texto, String etiqueta) {
        int index = texto.indexOf(etiqueta);
        if (index == -1) return null;
        int nextLabel = texto.indexOf("\n", index + etiqueta.length());
        if (nextLabel == -1) nextLabel = texto.length();
        return texto.substring(index + etiqueta.length(), nextLabel).trim();
    }

    private SugerenciaIA generarSugerenciaSimulada(SolicitudAcademica solicitud, List<TipoSolicitud> tipos) {
        log.info("Generando sugerencia simulada (Fallback)");
        TipoSolicitud sugerido = tipos.get(0);
        String desc = solicitud.getDescripcion().toLowerCase();
        for (TipoSolicitud t : tipos) {
            if (desc.contains(t.getNombre().toLowerCase())) {
                sugerido = t;
                break;
            }
        }

        SugerenciaIA sugerencia = new SugerenciaIA();
        sugerencia.setSolicitud(solicitud);
        sugerencia.setTipoSugerido(sugerido);
        sugerencia.setPrioridadSugerida(triageService.evaluarPrioridad(solicitud));
        sugerencia.setConfianza(0.60);
        sugerencia.setJustificacionIA("Simulación: Análisis básico por palabras clave (API Key no configurada).");
        return sugerenciaRepository.save(sugerencia);
    }
}
