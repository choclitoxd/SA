import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TiposSolicitudService } from './tipos-solicitud.service';
import { TipoSolicitudResponse, CrearTipoSolicitudRequest, CategoriaSolicitud } from '../models';

describe('TiposSolicitudService', () => {
  let service: TiposSolicitudService;
  let httpMock: HttpTestingController;

  const mockTipo: TipoSolicitudResponse = {
    id: 1,
    nombre: 'Solicitud de Matrícula',
    descripcion: 'Proceso de matrícula',
    tiempoAtencionDias: 5,
    activo: true,
    categoria: CategoriaSolicitud.GESTION_CURRICULAR,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        TiposSolicitudService,
      ],
    });
    service = TestBed.inject(TiposSolicitudService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('listar()', () => {
    it('should GET /tipos-solicitud with no params when called without arguments', () => {
      service.listar().subscribe(result => {
        expect(result).toEqual([mockTipo]);
      });
      const req = httpMock.expectOne(r => r.url === '/tipos-solicitud');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.has('activo')).toBeFalse();
      req.flush([mockTipo]);
    });

    it('should GET /tipos-solicitud?activo=true when called with activo=true', () => {
      service.listar(true).subscribe(result => {
        expect(result).toEqual([mockTipo]);
      });
      const req = httpMock.expectOne(r => r.url === '/tipos-solicitud');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('activo')).toBe('true');
      req.flush([mockTipo]);
    });

    it('should GET /tipos-solicitud?activo=false when called with activo=false', () => {
      service.listar(false).subscribe();
      const req = httpMock.expectOne(r => r.url === '/tipos-solicitud');
      expect(req.request.params.get('activo')).toBe('false');
      req.flush([]);
    });

    it('should return an array of tipos', () => {
      let result: TipoSolicitudResponse[] | undefined;
      service.listar().subscribe(r => result = r);
      httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
      expect(result!.length).toBe(1);
      expect(result![0].nombre).toBe('Solicitud de Matrícula');
    });
  });

  describe('crear()', () => {
    it('should POST to /tipos-solicitud with request body', () => {
      const reqBody: CrearTipoSolicitudRequest = {
        nombre: 'Nuevo Tipo',
        descripcion: 'Descripcion del tipo',
        tiempoAtencionDias: 3,
        categoria: CategoriaSolicitud.DOCUMENTACION,
      };
      service.crear(reqBody).subscribe(result => {
        expect(result).toEqual(mockTipo);
      });
      const req = httpMock.expectOne('/tipos-solicitud');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockTipo);
    });
  });

  describe('actualizar()', () => {
    it('should PUT to /tipos-solicitud/1 with request body', () => {
      const reqBody: CrearTipoSolicitudRequest = {
        nombre: 'Tipo Actualizado',
        tiempoAtencionDias: 7,
        categoria: CategoriaSolicitud.GESTION_CURRICULAR,
      };
      service.actualizar(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockTipo);
      });
      const req = httpMock.expectOne('/tipos-solicitud/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockTipo);
    });

    it('should use the correct id in the URL', () => {
      service.actualizar(5, { nombre: 'Test', tiempoAtencionDias: 3, categoria: CategoriaSolicitud.OTROS }).subscribe();
      const req = httpMock.expectOne('/tipos-solicitud/5');
      expect(req.request.method).toBe('PUT');
      req.flush(mockTipo);
    });
  });

  describe('desactivar()', () => {
    it('should DELETE /tipos-solicitud/1', () => {
      service.desactivar(1).subscribe();
      const req = httpMock.expectOne('/tipos-solicitud/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should use the correct id for deletion', () => {
      service.desactivar(3).subscribe();
      const req = httpMock.expectOne('/tipos-solicitud/3');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
