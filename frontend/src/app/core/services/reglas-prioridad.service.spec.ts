import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ReglasPrioridadService } from './reglas-prioridad.service';
import { ReglaPrioridad, NivelPrioridad } from '../models';

describe('ReglasPrioridadService', () => {
  let service: ReglasPrioridadService;
  let httpMock: HttpTestingController;

  const mockRegla: ReglaPrioridad = {
    id: 1,
    nombre: 'Regla CRITICA',
    descripcion: 'Regla para solicitudes críticas',
    condicion: 'tipo == DOCUMENTACION && diasRestantes < 2',
    nivelResultante: NivelPrioridad.CRITICA,
    peso: 100,
    activa: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ReglasPrioridadService,
      ],
    });
    service = TestBed.inject(ReglasPrioridadService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('listar()', () => {
    it('should GET /reglas-prioridad', () => {
      service.listar().subscribe(result => {
        expect(result).toEqual([mockRegla]);
      });
      const req = httpMock.expectOne('/reglas-prioridad');
      expect(req.request.method).toBe('GET');
      req.flush([mockRegla]);
    });

    it('should return an array of reglas', () => {
      let result: ReglaPrioridad[] | undefined;
      service.listar().subscribe(r => result = r);
      httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
      expect(result!.length).toBe(1);
      expect(result![0].nombre).toBe('Regla CRITICA');
    });

    it('should return empty array when no rules exist', () => {
      let result: ReglaPrioridad[] | undefined;
      service.listar().subscribe(r => result = r);
      httpMock.expectOne('/reglas-prioridad').flush([]);
      expect(result!.length).toBe(0);
    });
  });

  describe('crear()', () => {
    it('should POST to /reglas-prioridad with request body', () => {
      const newRegla: ReglaPrioridad = {
        nombre: 'Nueva Regla',
        condicion: 'prioridad == ALTA',
        nivelResultante: NivelPrioridad.ALTA,
        peso: 50,
        activa: true,
      };
      service.crear(newRegla).subscribe(result => {
        expect(result).toEqual(mockRegla);
      });
      const req = httpMock.expectOne('/reglas-prioridad');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newRegla);
      req.flush(mockRegla);
    });
  });

  describe('actualizar()', () => {
    it('should PUT to /reglas-prioridad/1 with request body', () => {
      const updatedRegla: ReglaPrioridad = { ...mockRegla, nombre: 'Regla Actualizada' };
      service.actualizar(1, updatedRegla).subscribe(result => {
        expect(result).toEqual(updatedRegla);
      });
      const req = httpMock.expectOne('/reglas-prioridad/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updatedRegla);
      req.flush(updatedRegla);
    });

    it('should use the correct id in the URL', () => {
      service.actualizar(7, mockRegla).subscribe();
      const req = httpMock.expectOne('/reglas-prioridad/7');
      expect(req.request.method).toBe('PUT');
      req.flush(mockRegla);
    });
  });

  describe('eliminar()', () => {
    it('should DELETE /reglas-prioridad/1', () => {
      service.eliminar(1).subscribe();
      const req = httpMock.expectOne('/reglas-prioridad/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should use the correct id for deletion', () => {
      service.eliminar(10).subscribe();
      const req = httpMock.expectOne('/reglas-prioridad/10');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });
});
