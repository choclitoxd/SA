package com.universidad.pisc.catalogo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipos_solicitud", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nombre")
})
@Getter
@Setter
public class TipoSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @NotNull
    @Column(nullable = false)
    private Boolean activo = true;

    @NotNull
    @Min(1)
    @Max(90)
    @Column(nullable = false)
    private Integer tiempoAtencionDias;
}
