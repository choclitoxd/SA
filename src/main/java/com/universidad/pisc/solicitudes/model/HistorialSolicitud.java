package com.universidad.pisc.solicitudes.model;

import com.universidad.pisc.identidad.model.Usuario;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_solicitudes")
@Getter
@Setter
public class HistorialSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAcademica solicitud;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @NotNull
    @Column(nullable = false)
    private String accionRealizada;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estadoAnterior;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estadoNuevo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejecutado_por_id", nullable = false)
    private Usuario ejecutadaPor;

    @PrePersist
    protected void alCrear() {
        this.fechaHora = LocalDateTime.now();
    }
}
