import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { NombreRol } from '../models';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthService,
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should add Authorization header when user is logged in', () => {
    authService.setUser('admin@example.com', 'password123', {
      nombre: 'Admin',
      apellido: 'User',
      roles: [NombreRol.COORDINADOR],
    });

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    const expectedCredentials = btoa('admin@example.com:password123');
    expect(req.request.headers.has('Authorization')).toBeTrue();
    expect(req.request.headers.get('Authorization')).toBe(`Basic ${expectedCredentials}`);
    req.flush({});
  });

  it('should not add Authorization header when no user is logged in', () => {
    // Ensure no user
    expect(authService.currentUser()).toBeNull();

    httpClient.get('/api/test').subscribe();

    const req = httpMock.expectOne('/api/test');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('should pass request through even without credentials', () => {
    httpClient.get('/public/data').subscribe(result => {
      expect(result).toEqual({ data: 'public' });
    });

    const req = httpMock.expectOne('/public/data');
    req.flush({ data: 'public' });
  });

  it('should correctly encode the credentials as base64', () => {
    authService.setUser('user@test.com', 'mypassword', {
      nombre: 'User',
      apellido: 'Test',
      roles: [NombreRol.ESTUDIANTE],
    });

    httpClient.get('/api/data').subscribe();

    const req = httpMock.expectOne('/api/data');
    const authHeader = req.request.headers.get('Authorization');
    expect(authHeader).not.toBeNull();
    const base64Part = authHeader!.replace('Basic ', '');
    const decoded = atob(base64Part);
    expect(decoded).toBe('user@test.com:mypassword');
    req.flush({});
  });
});
