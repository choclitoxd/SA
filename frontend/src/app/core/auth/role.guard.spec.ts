import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';
import { NombreRol } from '../models';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('roleGuard', () => {
  let authService: AuthService;
  let router: Router;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/admin' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthService,
      ],
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should return true when user has the required role', () => {
    authService.setUser('coord@example.com', 'password', {
      nombre: 'Coordinador',
      apellido: 'Test',
      roles: [NombreRol.COORDINADOR],
    });

    const guard = roleGuard(NombreRol.COORDINADOR);
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(result).toBeTrue();
  });

  it('should return true when user has any of multiple required roles', () => {
    authService.setUser('admin@example.com', 'password', {
      nombre: 'Admin',
      apellido: 'User',
      roles: [NombreRol.ADMINISTRATIVO],
    });

    const guard = roleGuard(NombreRol.COORDINADOR, NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR);
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(result).toBeTrue();
  });

  it('should return false when user lacks the required role', () => {
    authService.setUser('student@example.com', 'password', {
      nombre: 'Estudiante',
      apellido: 'Test',
      roles: [NombreRol.ESTUDIANTE],
    });

    const guard = roleGuard(NombreRol.COORDINADOR);
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(result).toBeFalse();
  });

  it('should navigate to / when user lacks the required role', () => {
    authService.setUser('student@example.com', 'password', {
      nombre: 'Estudiante',
      apellido: 'Test',
      roles: [NombreRol.ESTUDIANTE],
    });

    const navigateSpy = spyOn(router, 'navigate');
    const guard = roleGuard(NombreRol.COORDINADOR);
    TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('should return false when user is not authenticated', () => {
    expect(authService.currentUser()).toBeNull();

    const guard = roleGuard(NombreRol.COORDINADOR);
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(result).toBeFalse();
  });

  it('should navigate to /login when user is not authenticated', () => {
    const navigateSpy = spyOn(router, 'navigate');

    const guard = roleGuard(NombreRol.COORDINADOR);
    TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should NOT navigate to /login when user is authenticated but lacks role', () => {
    authService.setUser('docente@example.com', 'password', {
      nombre: 'Docente',
      apellido: 'Test',
      roles: [NombreRol.DOCENTE],
    });

    const navigateSpy = spyOn(router, 'navigate');
    const guard = roleGuard(NombreRol.COORDINADOR);
    TestBed.runInInjectionContext(() =>
      guard(mockRoute, mockState)
    );

    expect(navigateSpy).not.toHaveBeenCalledWith(['/login']);
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
});
