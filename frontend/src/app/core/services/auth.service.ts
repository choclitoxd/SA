import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { NombreRol, UsuarioResponse } from '../models';

export interface AuthUser {
  email: string;
  nombre: string;
  apellido: string;
  roles: NombreRol[];
  credentials: string; // base64 encoded email:password
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = '';
  private readonly STORAGE_KEY = 'pisc_auth';

  currentUser = signal<AuthUser | null>(this.loadFromStorage());

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<UsuarioResponse[]> {
    const credentials = btoa(`${email}:${password}`);
    const headers = { Authorization: `Basic ${credentials}` };

    return this.http.get<any>(`${this.API}/usuarios`, { headers, params: { page: 0, size: 1 } }).pipe(
      tap(() => {
        // fetch self - we use /usuarios with the given credentials
        // Since there's no /me endpoint, we'll store credentials and fetch user list
        // to verify auth works, then we store the credentials
      }),
      catchError(err => throwError(() => err))
    );
  }

  loginAndFetchProfile(email: string, password: string): Observable<UsuarioResponse[]> {
    const credentials = btoa(`${email}:${password}`);
    const headers = { Authorization: `Basic ${credentials}` };

    return this.http.get<any>(`${this.API}/solicitudes`, { headers, params: { page: 0, size: 1 } }).pipe(
      tap((resp) => {
        // Auth is valid - store temporary credentials until we know who the user is
      }),
      catchError(err => throwError(() => err))
    );
  }

  setUser(email: string, password: string, userData: { nombre: string; apellido: string; roles: NombreRol[] }): void {
    const credentials = btoa(`${email}:${password}`);
    const user: AuthUser = {
      email,
      nombre: userData.nombre,
      apellido: userData.apellido,
      roles: userData.roles,
      credentials,
    };
    this.currentUser.set(user);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
  }

  logout(): void {
    this.currentUser.set(null);
    localStorage.removeItem(this.STORAGE_KEY);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }

  hasRole(role: NombreRol): boolean {
    return this.currentUser()?.roles.includes(role) ?? false;
  }

  hasAnyRole(...roles: NombreRol[]): boolean {
    const userRoles = this.currentUser()?.roles ?? [];
    return roles.some(r => userRoles.includes(r));
  }

  getCredentials(): string | null {
    return this.currentUser()?.credentials ?? null;
  }

  private loadFromStorage(): AuthUser | null {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }
}
