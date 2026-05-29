import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  SolicitudDetalleResponse,
  SolicitudResumen,
  RegistrarSolicitudRequest,
  ClasificarSolicitudRequest,
  AsignarResponsableRequest,
  MarcarAtendidaRequest,
  CerrarSolicitudRequest,
  RechazarSolicitudRequest,
} from '../models/solicitud.model';
import { PageResponse } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class SolicitudesService {
  private readonly API = '/solicitudes';

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10): Observable<PageResponse<SolicitudResumen>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<SolicitudResumen>>(this.API, { params });
  }

  obtener(id: number): Observable<SolicitudDetalleResponse> {
    return this.http.get<SolicitudDetalleResponse>(`${this.API}/${id}`);
  }

  registrar(req: RegistrarSolicitudRequest): Observable<SolicitudDetalleResponse> {
    return this.http.post<SolicitudDetalleResponse>(this.API, req);
  }

  clasificar(id: number, req: ClasificarSolicitudRequest): Observable<SolicitudDetalleResponse> {
    return this.http.post<SolicitudDetalleResponse>(`${this.API}/${id}/clasificar`, req);
  }

  asignar(id: number, req: AsignarResponsableRequest): Observable<SolicitudDetalleResponse> {
    return this.http.post<SolicitudDetalleResponse>(`${this.API}/${id}/asignar`, req);
  }

  marcarAtendida(id: number, req: MarcarAtendidaRequest): Observable<SolicitudDetalleResponse> {
    return this.http.post<SolicitudDetalleResponse>(`${this.API}/${id}/atender`, req);
  }

  cerrar(id: number, req: CerrarSolicitudRequest): Observable<SolicitudDetalleResponse> {
    return this.http.post<SolicitudDetalleResponse>(`${this.API}/${id}/cerrar`, req);
  }

  rechazar(id: number, req: RechazarSolicitudRequest): Observable<SolicitudDetalleResponse> {
    return this.http.post<SolicitudDetalleResponse>(`${this.API}/${id}/rechazar`, req);
  }
}
