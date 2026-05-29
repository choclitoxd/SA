import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { NombreRol } from '../models';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('authGuard', () => {
  let authService: AuthService;
  let router: Router;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/dashboard' } as RouterStateSnapshot;

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

  it('should return true when user is authenticated', () => {
    authService.setUser('user@example.com', 'password', {
      nombre: 'User',
      apellido: 'Test',
      roles: [NombreRol.ESTUDIANTE],
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    expect(result).toBeTrue();
  });

  it('should return false when user is not authenticated', () => {
    expect(authService.currentUser()).toBeNull();

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    expect(result).toBeFalse();
  });

  it('should navigate to /login when user is not authenticated', () => {
    const navigateSpy = spyOn(router, 'navigate');

    TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should NOT navigate when user is authenticated', () => {
    authService.setUser('user@example.com', 'password', {
      nombre: 'User',
      apellido: 'Test',
      roles: [NombreRol.COORDINADOR],
    });

    const navigateSpy = spyOn(router, 'navigate');

    TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState)
    );

    expect(navigateSpy).not.toHaveBeenCalled();
  });
});
