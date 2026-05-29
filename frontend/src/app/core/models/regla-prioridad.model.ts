import { NivelPrioridad } from './enums';

export interface ReglaPrioridad {
  id?: number;
  version?: number;
  nombre: string;
  descripcion?: string;
  condicion: string;
  nivelResultante: NivelPrioridad;
  peso: number;
  activa: boolean;
}
