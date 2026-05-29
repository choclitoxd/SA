import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UsuariosService } from './usuarios.service';
import { UsuarioResponse, PageResponse, NombreRol, CrearUsuarioRequest, ActualizarUsuarioRequest } from '../models';

describe('UsuariosService', () => {
  let service: UsuariosService;
  let httpMock: HttpTestingController;

  const mockUsuario: UsuarioResponse = {
    id: 1,
    nombre: 'María',
    apellido: 'García',
    email: 'maria@example.com',
    identificacion: '987654321',
    roles: [NombreRol.COORDINADOR],
    activo: true,
    creadoEn: '2024-01-01T00:00:00',
  };

  const mockPage: PageResponse<UsuarioResponse> = {
    content: [mockUsuario],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 20,
    first: true,
    last: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        UsuariosService,
      ],
    });
    service = TestBed.inject(UsuariosService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('listar()', () => {
    it('should GET /usuarios with page=0 and size=20 by default', () => {
      service.listar().subscribe(result => {
        expect(result).toEqual(mockPage);
      });
      const req = httpMock.expectOne(r => r.url === '/usuarios');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('20');
      req.flush(mockPage);
    });

    it('should accept custom page and size parameters', () => {
      service.listar(1, 5).subscribe();
      const req = httpMock.expectOne(r => r.url === '/usuarios');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('size')).toBe('5');
      req.flush(mockPage);
    });

    it('should return the page response', () => {
      let result: PageResponse<UsuarioResponse> | undefined;
      service.listar().subscribe(r => result = r);
      httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
      expect(result!.content[0].email).toBe('maria@example.com');
    });
  });

  describe('obtener()', () => {
    it('should GET /usuarios/1', () => {
      service.obtener(1).subscribe(result => {
        expect(result).toEqual(mockUsuario);
      });
      const req = httpMock.expectOne('/usuarios/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockUsuario);
    });

    it('should use the correct id in the URL', () => {
      service.obtener(99).subscribe();
      const req = httpMock.expectOne('/usuarios/99');
      expect(req.request.method).toBe('GET');
      req.flush(mockUsuario);
    });
  });

  describe('crear()', () => {
    it('should POST to /usuarios with request body', () => {
      const reqBody: CrearUsuarioRequest = {
        nombre: 'Carlos',
        apellido: 'Lopez',
        email: 'carlos@example.com',
        identificacion: '111222333',
        roles: [NombreRol.DOCENTE],
        password: 'securePass123',
      };
      service.crear(reqBody).subscribe(result => {
        expect(result).toEqual(mockUsuario);
      });
      const req = httpMock.expectOne('/usuarios');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockUsuario);
    });
  });

  describe('actualizar()', () => {
    it('should PUT to /usuarios/1 with request body', () => {
      const reqBody: ActualizarUsuarioRequest = {
        nombre: 'Maria Updated',
        roles: [NombreRol.COORDINADOR, NombreRol.DOCENTE],
      };
      service.actualizar(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockUsuario);
      });
      const req = httpMock.expectOne('/usuarios/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockUsuario);
    });

    it('should use the correct id in the URL for update', () => {
      service.actualizar(42, { nombre: 'Test' }).subscribe();
      const req = httpMock.expectOne('/usuarios/42');
      expect(req.request.method).toBe('PUT');
      req.flush(mockUsuario);
    });
  });

  describe('desactivar()', () => {
    it('should DELETE /usuarios/1', () => {
      service.desactivar(1).subscribe();
      const req = httpMock.expectOne('/usuarios/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should use the correct id for deletion', () => {
      service.desactivar(7).subscribe();
      const req = httpMock.expectOne('/usuarios/7');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
