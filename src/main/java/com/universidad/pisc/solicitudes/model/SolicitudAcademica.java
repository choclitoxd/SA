package com.universidad.pisc.solicitudes.model;

import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.identidad.model.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
public class SolicitudAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, updatable = false, length = 30)
    private String codigo;

    @NotNull
    @Size(min = 30, max = 2000)
    @Column(length = 2000, nullable = false)
    private String descripcion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalOrigen canal;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaUltimaActualizacion;

    private LocalDateTime fechaLimite;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.REGISTRADA;

    @Size(min = 20, max = 1000)
    @Column(length = 1000)
    private String observacionCierre;

    @NotNull
    @Column(nullable = false)
    private Integer contadorReaperturas = 0;

    @NotNull
    @Column(nullable = false)
    private Integer contadorReclasificaciones = 0;

    // --- Relaciones ---

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_solicitud_id")
    private TipoSolicitud tipo;

    @Embedded
    private Prioridad prioridad;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Asignacion> asignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HistorialSolicitud> historial = new ArrayList<>();

    @OneToOne(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private SugerenciaIA sugerenciaIA;


    @PrePersist
    protected void alCrear() {
        // Genera un código único simple antes de guardar.
        // Una implementación más robusta podría usar una secuencia de base de datos.
        if (this.codigo == null) {
            this.codigo = String.format("SOL-%d-%d", LocalDateTime.now().getYear(), System.currentTimeMillis() % 100000);
        }
    }
}
