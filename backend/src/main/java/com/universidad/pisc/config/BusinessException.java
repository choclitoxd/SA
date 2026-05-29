package com.universidad.pisc.config;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
    * Error de dominio que interrumpe el flujo cuando se viola una regla de negocio.
*/
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(String mensaje) {
        this(mensaje, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String mensaje, HttpStatus status) {
        super(mensaje);
        this.status = status;
    }
}
