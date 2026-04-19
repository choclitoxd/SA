package com.universidad.pisc.solicitudes.model;

import com.universidad.pisc.catalogo.model.TipoSolicitud;
import com.universidad.pisc.config.BusinessException;
import com.universidad.pisc.identidad.model.Usuario;
import com.universidad.pisc.solicitudes.enums.CanalOrigen;
import com.universidad.pisc.solicitudes.enums.EstadoSolicitud;
import com.universidad.pisc.solicitudes.enums.MotivoRechazo;

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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitudes")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class SolicitudAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @CreatedBy
    @Column(updatable = false)
    private String creadoPor;

    @LastModifiedBy
    private String actualizadoPor;

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

    @Enumerated(EnumType.STRING)
    private MotivoRechazo motivoRechazo;

    @Size(min = 20, max = 2000)
    @Column(length = 2000)
    private String observacionResolucion;

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

    // --- Comportamiento de Dominio (Transiciones de Estado) ---
    /**
        * Valida y ejecuta el cambio a estado CLASIFICADA asignando tipo y prioridad. */
    public void clasificar(TipoSolicitud tipo, Prioridad prioridad) {
        validarEstado(EstadoSolicitud.REGISTRADA);
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.estado = EstadoSolicitud.CLASIFICADA;
    }
     /**
        * Transiciona la solicitud a EN_ATENCION al asignar un responsable.
    */
    public void asignar(Usuario responsable) {
        validarEstado(EstadoSolicitud.CLASIFICADA, EstadoSolicitud.EN_ATENCION);
        this.estado = EstadoSolicitud.EN_ATENCION;
    }
    /**
        * Registra la solución técnica y mueve la solicitud a estado ATENDIDA.
        * @throws BusinessException si la resolución es demasiado breve.
    */
    public void marcarAtendida(String resolucion) {
        validarEstado(EstadoSolicitud.EN_ATENCION);
        if (resolucion == null || resolucion.trim().length() < 20) {
            throw new com.universidad.pisc.config.BusinessException("La resolución debe tener al menos 20 caracteres.");
        }
        this.observacionResolucion = resolucion;
        this.estado = EstadoSolicitud.ATENDIDA;
    }
    /**
        * Finaliza el ciclo de vida de la solicitud tras la conformidad del cierre.
    */
    public void cerrar(String observacion) {
        validarEstado(EstadoSolicitud.ATENDIDA);
        if (observacion == null || observacion.trim().length() < 20) {
            throw new com.universidad.pisc.config.BusinessException("La observación de cierre debe tener al menos 20 caracteres.");
        }
        this.observacionCierre = observacion;
        this.estado = EstadoSolicitud.CERRADA;
    }
    /**
        * Cancela el proceso de la solicitud por motivos administrativos o falta de requisitos.  
    */
    public void rechazar(MotivoRechazo motivo, String justificacion) {
        validarEstado(EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA);
        if (justificacion == null || justificacion.trim().length() < 20) {
            throw new com.universidad.pisc.config.BusinessException("La justificación del rechazo debe tener al menos 20 caracteres.");
        }
        this.motivoRechazo = motivo;
        this.observacionResolucion = justificacion;
        this.estado = EstadoSolicitud.RECHAZADA;
    }
        /**
            * Permite reabrir una solicitud cerrada para correcciones o información adicional.
        */
    private void validarEstado(EstadoSolicitud... permitidos) {
        boolean esValido = false;
        for (EstadoSolicitud p : permitidos) {
            if (this.estado == p) {
                esValido = true;
                break;
            }
        }
        if (!esValido) {
            throw new com.universidad.pisc.config.BusinessException(
                String.format("Transición no permitida: la solicitud está en estado %s", this.estado)
            );
        }
    }

    @PrePersist
    protected void alCrear() {
        // Genera un código único simple antes de guardar.
        // Una implementación más robusta podría usar una secuencia de base de datos.
        if (this.codigo == null) {
            this.codigo = String.format("SOL-%d-%d", LocalDateTime.now().getYear(), System.currentTimeMillis() % 1000000);
        }
    }

    /**
     * Comprueba si la solicitud está vencida (fecha límite pasada y estado no final).
     */
    public boolean isVencida() {
        if (this.fechaLimite == null || this.estado.isFinal()) {
            return false;
        }
        return this.fechaLimite.isBefore(LocalDateTime.now());
    }

    /**
     * Retorna el responsable actual de la solicitud (proyección de la asignación activa).
     */
    public Usuario getResponsableActual() {
        return this.asignaciones.stream()
                .filter(Asignacion::getActiva)
                .map(Asignacion::getResponsable)
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper para el motor de reglas: retorna el nombre del tipo de solicitud.
     */
    public String getTipoNombre() {
        return this.tipo != null ? this.tipo.getNombre() : null;
    }

    /**
     * Helper para el motor de reglas: retorna los días restantes hasta la fecha límite.
     */
    public Long getDiasRestantes() {
        if (this.fechaLimite == null) {
            return null;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), this.fechaLimite);
    }
}
