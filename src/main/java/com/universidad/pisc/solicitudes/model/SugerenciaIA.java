package com.universidad.pisc.solicitudes.model;

import com.universidad.pisc.catalogo.model.NivelPrioridad;
import com.universidad.pisc.catalogo.model.TipoSolicitud;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sugerencias_ia")
@Getter
@Setter
public class SugerenciaIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Inmutable una vez generada, no necesita @Version

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private SolicitudAcademica solicitud;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "tipo_sugerido_id", nullable = false)
    private TipoSolicitud tipoSugerido;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelPrioridad prioridadSugerida;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String justificacionIA;

    @NotNull
    @Min(0)
    @Max(1)
    @Column(nullable = false)
    private Double confianza;

    @NotNull
    @Column(nullable = false)
    private Boolean confirmada = false;

    @NotNull
    @Column(nullable = false)
    private Boolean ajustada = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime generadaEn;

    @PrePersist
    protected void alCrear() {
        this.generadaEn = LocalDateTime.now();
    }
}
