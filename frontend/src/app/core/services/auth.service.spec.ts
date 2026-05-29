import { TestBed } from '@angular/core/testing';
import { provideHttpClient, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { NombreRol } from '../models';

describe('AuthService', () => {
  let service: AuthService;
  let router: Router;

  const mockUser = {
    email: 'test@example.com',
    nombre: 'Test',
    apellido: 'User',
    roles: [NombreRol.COORDINADOR],
    credentials: btoa('test@example.com:password123'),
  };

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
    service = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('isAuthenticated()', () => {
    it('should return false when no user is set', () => {
      expect(service.isAuthenticated()).toBeFalse();
    });

    it('should return true after setUser() is called', () => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR],
      });
      expect(service.isAuthenticated()).toBeTrue();
    });
  });

  describe('hasRole()', () => {
    beforeEach(() => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR],
      });
    });

    it('should return true for a role the user has', () => {
      expect(service.hasRole(NombreRol.COORDINADOR)).toBeTrue();
    });

    it('should return false for a role the user does not have', () => {
      expect(service.hasRole(NombreRol.ESTUDIANTE)).toBeFalse();
    });

    it('should return false when no user is set', () => {
      service.logout();
      expect(service.hasRole(NombreRol.COORDINADOR)).toBeFalse();
    });
  });

  describe('hasAnyRole()', () => {
    beforeEach(() => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR, NombreRol.DOCENTE],
      });
    });

    it('should return true if user has any of the specified roles', () => {
      expect(service.hasAnyRole(NombreRol.ESTUDIANTE, NombreRol.COORDINADOR)).toBeTrue();
    });

    it('should return true when exactly one role matches', () => {
      expect(service.hasAnyRole(NombreRol.DOCENTE)).toBeTrue();
    });

    it('should return false when none of the specified roles match', () => {
      expect(service.hasAnyRole(NombreRol.ESTUDIANTE, NombreRol.ADMINISTRATIVO)).toBeFalse();
    });

    it('should return false when no user is set', () => {
      service.logout();
      expect(service.hasAnyRole(NombreRol.COORDINADOR)).toBeFalse();
    });
  });

  describe('setUser()', () => {
    it('should update the currentUser signal', () => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR],
      });
      const user = service.currentUser();
      expect(user).not.toBeNull();
      expect(user!.email).toBe('test@example.com');
      expect(user!.nombre).toBe('Test');
      expect(user!.apellido).toBe('User');
      expect(user!.roles).toContain(NombreRol.COORDINADOR);
    });

    it('should store user in localStorage', () => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR],
      });
      const stored = localStorage.getItem('pisc_auth');
      expect(stored).not.toBeNull();
      const parsed = JSON.parse(stored!);
      expect(parsed.email).toBe('test@example.com');
    });

    it('should encode credentials as base64', () => {
      service.setUser('test@example.com', 'mypassword', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.ESTUDIANTE],
      });
      const expectedCredentials = btoa('test@example.com:mypassword');
      expect(service.currentUser()!.credentials).toBe(expectedCredentials);
    });
  });

  describe('logout()', () => {
    beforeEach(() => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR],
      });
    });

    it('should clear the currentUser signal', () => {
      service.logout();
      expect(service.currentUser()).toBeNull();
    });

    it('should remove user from localStorage', () => {
      service.logout();
      expect(localStorage.getItem('pisc_auth')).toBeNull();
    });

    it('should navigate to /login', () => {
      const navigateSpy = spyOn(router, 'navigate');
      service.logout();
      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('getCredentials()', () => {
    it('should return null when no user is set', () => {
      expect(service.getCredentials()).toBeNull();
    });

    it('should return base64 encoded credentials when user is set', () => {
      service.setUser('test@example.com', 'password123', {
        nombre: 'Test',
        apellido: 'User',
        roles: [NombreRol.COORDINADOR],
      });
      const expected = btoa('test@example.com:password123');
      expect(service.getCredentials()).toBe(expected);
    });
  });

  describe('localStorage persistence', () => {
    it('should load user from localStorage on init', () => {
      localStorage.setItem('pisc_auth', JSON.stringify(mockUser));
      // Re-create service so it reads from storage
      const newService = new AuthService(
        TestBed.inject(HttpClient),
        router
      );
      expect(newService.currentUser()).not.toBeNull();
      expect(newService.currentUser()!.email).toBe('test@example.com');
    });

    it('should return null when localStorage is empty', () => {
      localStorage.clear();
      expect(service.currentUser()).toBeNull();
    });

    it('should gracefully handle invalid JSON in localStorage', () => {
      localStorage.setItem('pisc_auth', 'invalid-json{{{');
      // Re-instantiate to trigger loadFromStorage
      const freshService = new AuthService(
        TestBed.inject(HttpClient),
        router
      );
      expect(freshService.currentUser()).toBeNull();
    });
  });
});
