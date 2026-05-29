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
 * Utiliza modelos de lenguaje para categorizar semánticamente las solicitudes académicas.
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

    /**
     * Inicializa el cliente de Gemini validando la presencia de la API Key.
     */
    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("TU_API_KEY")) {
            log.error("API Key de Gemini no configurada correctamente.");
            this.model = null;
        } else {
            this.model = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey.trim())
                    .modelName("gemini-2.5-flash") // Modelo estable confirmado para 2026
                    .logRequestsAndResponses(true)
                    .build();
        }
    }

    /**
     * Genera una sugerencia inteligente basada en la descripción. 
     * Implementa fallback automático a palabras clave si el modelo falla.
     */
    @Override
    @Transactional
    public SugerenciaIA generarSugerencia(SolicitudAcademica solicitud) {
        try {
            if (model != null) {
                List<TipoSolicitud> tiposActivos = tipoRepository.findByActivo(true);
                if (!tiposActivos.isEmpty()) {
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
                }
            }
        } catch (Exception e) {
            log.error("Error en Gemini: {}. Activando fallback simulado.", e.getMessage());
        }

        return generarSugerenciaFallback(solicitud);
    }

    /**
     * Clasificación de contingencia basada en coincidencia de términos exactos.
     */
    private SugerenciaIA generarSugerenciaFallback(SolicitudAcademica solicitud) {
        log.info("Generando sugerencia por fallback (Palabras clave)");
        List<TipoSolicitud> tipos = tipoRepository.findByActivo(true);
        if (tipos.isEmpty()) return null;

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
        sugerencia.setConfianza(0.50);
        sugerencia.setJustificacionIA("Clasificación por palabras clave (Fallback: Gemini no disponible).");

        return sugerenciaRepository.save(sugerencia);
    }

    /**
     * Parsea la respuesta estructurada del LLM y la vincula con los tipos del catálogo.
     */
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
