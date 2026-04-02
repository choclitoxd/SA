package com.universidad.pisc.solicitudes.model;

import com.universidad.pisc.catalogo.model.NivelPrioridad;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Value Object que representa la prioridad asignada a una solicitud.
 * Al ser @Embeddable, se almacena como columnas en la misma tabla de SolicitudAcademica.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prioridad {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad_nivel")
    private NivelPrioridad nivel;

    @NotNull
    @Size(min = 20, max = 500)
    @Column(name = "prioridad_justificacion", length = 500)
    private String justificacion;

    @NotNull
    @Column(name = "prioridad_asignada_en")
    private LocalDateTime asignadaEn;
}
