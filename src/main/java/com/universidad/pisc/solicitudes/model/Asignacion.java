package com.universidad.pisc.solicitudes.model;

import com.universidad.pisc.identidad.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones")
@Getter
@Setter
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Como indica el UML, el ciclo de vida de Asignacion está controlado por SolicitudAcademica,
    // por lo que no necesita su propio campo @Version.

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudAcademica solicitud;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por_id", nullable = false)
    private Usuario asignadoPor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime asignadaEn;

    @NotNull
    @Column(nullable = false)
    private Boolean activa = true;

    @Size(max = 500)
    @Column(length = 500)
    private String notas;

    @PrePersist
    protected void alCrear() {
        this.asignadaEn = LocalDateTime.now();
    }
}
