import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TipoSolicitudResponse, CrearTipoSolicitudRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class TiposSolicitudService {
  private readonly API = '/tipos-solicitud';

  constructor(private http: HttpClient) {}

  listar(activo?: boolean): Observable<TipoSolicitudResponse[]> {
    const params: any = {};
    if (activo !== undefined) params['activo'] = activo;
    return this.http.get<TipoSolicitudResponse[]>(this.API, { params });
  }

  crear(req: CrearTipoSolicitudRequest): Observable<TipoSolicitudResponse> {
    return this.http.post<TipoSolicitudResponse>(this.API, req);
  }

  actualizar(id: number, req: CrearTipoSolicitudRequest): Observable<TipoSolicitudResponse> {
    return this.http.put<TipoSolicitudResponse>(`${this.API}/${id}`, req);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
