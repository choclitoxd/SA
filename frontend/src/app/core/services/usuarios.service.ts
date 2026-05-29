import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  UsuarioResponse,
  CrearUsuarioRequest,
  ActualizarUsuarioRequest,
  PageResponse,
} from '../models';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly API = '/usuarios';

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 20): Observable<PageResponse<UsuarioResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<UsuarioResponse>>(this.API, { params });
  }

  obtener(id: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${this.API}/${id}`);
  }

  crear(req: CrearUsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(this.API, req);
  }

  actualizar(id: number, req: ActualizarUsuarioRequest): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${this.API}/${id}`, req);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
