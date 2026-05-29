package com.universidad.pisc.catalogo.model;

import com.universidad.pisc.catalogo.enums.NivelPrioridad;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reglas_prioridad")
@Getter
@Setter
public class ReglaPrioridad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;

    @NotNull
    @Column(nullable = false)
    private String condicion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelPrioridad nivelResultante;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer peso;

    @NotNull
    @Column(nullable = false)
    private Boolean activa = true;
}
