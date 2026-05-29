import { CategoriaSolicitud } from './enums';

export interface TipoSolicitudResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  tiempoAtencionDias: number;
  activo: boolean;
  categoria: CategoriaSolicitud;
}

export interface CrearTipoSolicitudRequest {
  nombre: string;
  descripcion?: string;
  tiempoAtencionDias: number;
  categoria: CategoriaSolicitud;
}
