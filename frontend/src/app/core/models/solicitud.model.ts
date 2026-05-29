import { CanalOrigen, EstadoSolicitud, MotivoRechazo, NivelPrioridad } from './enums';
import { TipoSolicitudResponse } from './tipo-solicitud.model';
import { UsuarioResumen } from './usuario.model';

export interface PrioridadResponse {
  nivel: NivelPrioridad;
  justificacion: string;
  asignadaEn: string;
}

export interface SugerenciaIAResponse {
  id: number;
  tipoSugerido: TipoSolicitudResponse;
  prioridadSugerida: NivelPrioridad;
  justificacionIA: string;
  confianza: number;
  confirmada: boolean;
  ajustada: boolean;
  generadaEn: string;
}

export interface HistorialEntry {
  id: number;
  fechaHora: string;
  accionRealizada: string;
  estadoAnterior?: EstadoSolicitud;
  estadoNuevo: EstadoSolicitud;
  observaciones?: string;
  usuarioResponsable: UsuarioResumen;
}

export interface SolicitudResumen {
  id: number;
  codigo: string;
  estado: EstadoSolicitud;
  tipoNombre?: string;
  prioridad?: NivelPrioridad;
  solicitanteNombre: string;
  responsableNombre?: string;
  fechaRegistro: string;
  fechaLimite?: string;
  vencida: boolean;
}

export interface SolicitudDetalleResponse {
  id: number;
  codigo: string;
  descripcion: string;
  canal: CanalOrigen;
  estado: EstadoSolicitud;
  tipo?: TipoSolicitudResponse;
  prioridad?: PrioridadResponse;
  solicitante: UsuarioResumen;
  responsableActual?: UsuarioResumen;
  contadorReaperturas: number;
  fechaRegistro: string;
  fechaUltimaActualizacion: string;
  fechaLimite?: string;
  observacionCierre?: string;
  sugerenciaIA?: SugerenciaIAResponse;
  version: number;
}

export interface RegistrarSolicitudRequest {
  descripcion: string;
  canal: CanalOrigen;
  solicitanteId: string;
  fechaLimite?: string;
}

export interface ClasificarSolicitudRequest {
  tipoSolicitudId: number;
  nivelPrioridad?: NivelPrioridad;
  justificacionPrioridad?: string;
  sugerenciaIaId?: number;
  version: number;
}

export interface AsignarResponsableRequest {
  responsableId: number;
  notas?: string;
  version: number;
}

export interface MarcarAtendidaRequest {
  observacionResolucion: string;
  version: number;
}

export interface CerrarSolicitudRequest {
  observacionCierre: string;
  version: number;
}

export interface RechazarSolicitudRequest {
  motivo: MotivoRechazo;
  justificacion: string;
  version: number;
}
