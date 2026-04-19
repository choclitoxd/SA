package com.universidad.pisc.solicitudes.service;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de clasificación usando Google Gemini.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "pisc.ia.provider", havingValue = "gemini")
public class GeminiClasificador implements SolicitudClasificador {

    private final TipoSolicitudRepository tipoRepository;
    private final SugerenciaIARepository sugerenciaRepository;
    private final TriageService triageService;

    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    private ChatLanguageModel model;

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("TU_API_KEY")) {
            log.error("API Key de Gemini no configurada correctamente.");
            this.model = null;
        } else {
            this.model = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey.trim())
                    .modelName("gemini-1.5-flash")
                    .logRequestsAndResponses(true)
                    .build();
        }
    }

    @Override
    @Transactional
    public SugerenciaIA generarSugerencia(SolicitudAcademica solicitud) {
        if (model == null) return null;

        List<TipoSolicitud> tiposActivos = tipoRepository.findByActivo(true);
        if (tiposActivos.isEmpty()) return null;

        try {
            String listaTipos = tiposActivos.stream()
                    .map(t -> "- " + t.getNombre() + ": " + t.getDescripcion())
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(
                    "Eres un experto académico. Clasifica esta solicitud:\n\"%s\"\n\n" +
                    "Tipos:\n%s\n\nResponde exactamente:\nTIPO: [Nombre]\nJUSTIFICACIÓN: [Breve]",
                    solicitud.getDescripcion(), listaTipos
            );

            String response = model.generate(prompt);
            return procesarRespuestaIA(response, solicitud, tiposActivos);
        } catch (Exception e) {
            log.error("Error en Gemini: {}", e.getMessage());
            return null;
        }
    }

    private SugerenciaIA procesarRespuestaIA(String response, SolicitudAcademica solicitud, List<TipoSolicitud> tipos) {
        String tipoNombre = extraerValor(response, "TIPO:");
        String justificacion = extraerValor(response, "JUSTIFICACIÓN:");

        TipoSolicitud sugerido = tipos.stream()
                .filter(t -> t.getNombre().equalsIgnoreCase(tipoNombre))
                .findFirst()
                .orElse(tipos.get(0));

        SugerenciaIA sugerencia = new SugerenciaIA();
        sugerencia.setSolicitud(solicitud);
        sugerencia.setTipoSugerido(sugerido);
        sugerencia.setPrioridadSugerida(triageService.evaluarPrioridad(solicitud));
        sugerencia.setConfianza(0.92);
        sugerencia.setJustificacionIA(justificacion != null ? justificacion : "Clasificación automática via Gemini.");

        return sugerenciaRepository.save(sugerencia);
    }

    private String extraerValor(String texto, String etiqueta) {
        int index = texto.indexOf(etiqueta);
        if (index == -1) return null;
        int end = texto.indexOf("\n", index + etiqueta.length());
        if (end == -1) end = texto.length();
        return texto.substring(index + etiqueta.length(), end).trim();
    }
}
