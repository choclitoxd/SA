package com.universidad.pisc.solicitudes.model;

import java.util.Set;

public enum EstadoSolicitud {
    REGISTRADA,
    CLASIFICADA,
    EN_ATENCION,
    ATENDIDA,
    CERRADA,
    RECHAZADA,
    CANCELADA,
    VENCIDA,
    ESCALADA,
    IMPUGNADA;

    // Según el diagrama UML, los estados finales son inmutables.
    private static final Set<EstadoSolicitud> ESTADOS_FINALES = Set.of(CERRADA, RECHAZADA, CANCELADA);

    /**
     * Comprueba si el estado actual es un estado final del ciclo de vida.
     * @return true si el estado es CERRADA, RECHAZADA o CANCELADA.
     */
    public boolean isFinal() {
        return ESTADOS_FINALES.contains(this);
    }
}
