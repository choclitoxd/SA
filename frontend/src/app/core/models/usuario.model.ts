import { NombreRol } from './enums';

export interface UsuarioResumen {
  id: number;
  nombre: string;
  apellido: string;
  identificacion: string;
  activo: boolean;
}

export interface UsuarioResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  identificacion: string;
  roles: NombreRol[];
  activo: boolean;
  creadoEn: string;
}

export interface CrearUsuarioRequest {
  nombre: string;
  apellido: string;
  email: string;
  identificacion: string;
  roles: NombreRol[];
  password: string;
}

export interface ActualizarUsuarioRequest {
  nombre?: string;
  apellido?: string;
  email?: string;
  roles?: NombreRol[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
