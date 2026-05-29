export enum EstadoSolicitud {
  REGISTRADA = 'REGISTRADA',
  CLASIFICADA = 'CLASIFICADA',
  EN_ATENCION = 'EN_ATENCION',
  ATENDIDA = 'ATENDIDA',
  CERRADA = 'CERRADA',
  RECHAZADA = 'RECHAZADA',
  CANCELADA = 'CANCELADA',
  VENCIDA = 'VENCIDA',
  ESCALADA = 'ESCALADA',
  IMPUGNADA = 'IMPUGNADA',
}

export enum NivelPrioridad {
  CRITICA = 'CRITICA',
  ALTA = 'ALTA',
  MEDIA = 'MEDIA',
  BAJA = 'BAJA',
}

export enum CanalOrigen {
  CSU = 'CSU',
  CORREO = 'CORREO',
  SAC = 'SAC',
  TELEFONICO = 'TELEFONICO',
  PRESENCIAL = 'PRESENCIAL',
  SISTEMA_ACADEMICO = 'SISTEMA_ACADEMICO',
}

export enum MotivoRechazo {
  DUPLICADA = 'DUPLICADA',
  INVALIDA = 'INVALIDA',
  FUERA_DE_ALCANCE = 'FUERA_DE_ALCANCE',
  INFORMACION_INSUFICIENTE = 'INFORMACION_INSUFICIENTE',
  PLAZO_VENCIDO = 'PLAZO_VENCIDO',
}

export enum CategoriaSolicitud {
  GESTION_CURRICULAR = 'GESTION_CURRICULAR',
  PERMANENCIA_ACADEMICA = 'PERMANENCIA_ACADEMICA',
  DOCUMENTACION = 'DOCUMENTACION',
  GRADOS_Y_EGRESADOS = 'GRADOS_Y_EGRESADOS',
  BIENESTAR_Y_APOYO = 'BIENESTAR_Y_APOYO',
  OTROS = 'OTROS',
}

export enum NombreRol {
  ESTUDIANTE = 'ESTUDIANTE',
  COORDINADOR = 'COORDINADOR',
  ADMINISTRATIVO = 'ADMINISTRATIVO',
  DIRECTOR = 'DIRECTOR',
  DOCENTE = 'DOCENTE',
}

export const ESTADO_LABELS: Record<EstadoSolicitud, string> = {
  [EstadoSolicitud.REGISTRADA]: 'Registrada',
  [EstadoSolicitud.CLASIFICADA]: 'Clasificada',
  [EstadoSolicitud.EN_ATENCION]: 'En Atención',
  [EstadoSolicitud.ATENDIDA]: 'Atendida',
  [EstadoSolicitud.CERRADA]: 'Cerrada',
  [EstadoSolicitud.RECHAZADA]: 'Rechazada',
  [EstadoSolicitud.CANCELADA]: 'Cancelada',
  [EstadoSolicitud.VENCIDA]: 'Vencida',
  [EstadoSolicitud.ESCALADA]: 'Escalada',
  [EstadoSolicitud.IMPUGNADA]: 'Impugnada',
};

export const PRIORIDAD_LABELS: Record<NivelPrioridad, string> = {
  [NivelPrioridad.CRITICA]: 'Crítica',
  [NivelPrioridad.ALTA]: 'Alta',
  [NivelPrioridad.MEDIA]: 'Media',
  [NivelPrioridad.BAJA]: 'Baja',
};

export const CANAL_LABELS: Record<CanalOrigen, string> = {
  [CanalOrigen.CSU]: 'CSU',
  [CanalOrigen.CORREO]: 'Correo',
  [CanalOrigen.SAC]: 'SAC',
  [CanalOrigen.TELEFONICO]: 'Telefónico',
  [CanalOrigen.PRESENCIAL]: 'Presencial',
  [CanalOrigen.SISTEMA_ACADEMICO]: 'Sistema Académico',
};

export const MOTIVO_LABELS: Record<MotivoRechazo, string> = {
  [MotivoRechazo.DUPLICADA]: 'Duplicada',
  [MotivoRechazo.INVALIDA]: 'Inválida',
  [MotivoRechazo.FUERA_DE_ALCANCE]: 'Fuera de Alcance',
  [MotivoRechazo.INFORMACION_INSUFICIENTE]: 'Información Insuficiente',
  [MotivoRechazo.PLAZO_VENCIDO]: 'Plazo Vencido',
};

export const CATEGORIA_LABELS: Record<CategoriaSolicitud, string> = {
  [CategoriaSolicitud.GESTION_CURRICULAR]: 'Gestión Curricular',
  [CategoriaSolicitud.PERMANENCIA_ACADEMICA]: 'Permanencia Académica',
  [CategoriaSolicitud.DOCUMENTACION]: 'Documentación',
  [CategoriaSolicitud.GRADOS_Y_EGRESADOS]: 'Grados y Egresados',
  [CategoriaSolicitud.BIENESTAR_Y_APOYO]: 'Bienestar y Apoyo',
  [CategoriaSolicitud.OTROS]: 'Otros',
};
