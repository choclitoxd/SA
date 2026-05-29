import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReglaPrioridad } from '../models';

@Injectable({ providedIn: 'root' })
export class ReglasPrioridadService {
  private readonly API = '/reglas-prioridad';

  constructor(private http: HttpClient) {}

  listar(): Observable<ReglaPrioridad[]> {
    return this.http.get<ReglaPrioridad[]>(this.API);
  }

  crear(req: ReglaPrioridad): Observable<ReglaPrioridad> {
    return this.http.post<ReglaPrioridad>(this.API, req);
  }

  actualizar(id: number, req: ReglaPrioridad): Observable<ReglaPrioridad> {
    return this.http.put<ReglaPrioridad>(`${this.API}/${id}`, req);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
