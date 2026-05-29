import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { SolicitudesService } from './solicitudes.service';
import {
  EstadoSolicitud,
  NivelPrioridad,
  CanalOrigen,
  MotivoRechazo,
} from '../models';
import {
  SolicitudResumen,
  SolicitudDetalleResponse,
  ClasificarSolicitudRequest,
  AsignarResponsableRequest,
  MarcarAtendidaRequest,
  CerrarSolicitudRequest,
  RechazarSolicitudRequest,
  RegistrarSolicitudRequest,
} from '../models/solicitud.model';
import { PageResponse } from '../models/usuario.model';

describe('SolicitudesService', () => {
  let service: SolicitudesService;
  let httpMock: HttpTestingController;

  const mockSolicitudResumen: SolicitudResumen = {
    id: 1,
    codigo: 'SOL-001',
    estado: EstadoSolicitud.REGISTRADA,
    solicitanteNombre: 'Juan Perez',
    fechaRegistro: '2024-01-15T10:00:00',
    vencida: false,
  };

  const mockPage: PageResponse<SolicitudResumen> = {
    content: [mockSolicitudResumen],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
    first: true,
    last: true,
  };

  const mockDetalle: SolicitudDetalleResponse = {
    id: 1,
    codigo: 'SOL-001',
    descripcion: 'Descripción de prueba',
    canal: CanalOrigen.CSU,
    estado: EstadoSolicitud.REGISTRADA,
    solicitante: { id: 1, nombre: 'Juan', apellido: 'Perez', identificacion: '123', activo: true },
    contadorReaperturas: 0,
    fechaRegistro: '2024-01-15T10:00:00',
    fechaUltimaActualizacion: '2024-01-15T10:00:00',
    version: 0,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        SolicitudesService,
      ],
    });
    service = TestBed.inject(SolicitudesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('listar()', () => {
    it('should GET /solicitudes with default page and size params', () => {
      service.listar().subscribe(result => {
        expect(result).toEqual(mockPage);
      });
      const req = httpMock.expectOne(r => r.url === '/solicitudes');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('0');
      expect(req.request.params.get('size')).toBe('10');
      req.flush(mockPage);
    });

    it('should GET /solicitudes with custom page and size', () => {
      service.listar(2, 20).subscribe();
      const req = httpMock.expectOne(r => r.url === '/solicitudes');
      expect(req.request.params.get('page')).toBe('2');
      expect(req.request.params.get('size')).toBe('20');
      req.flush(mockPage);
    });

    it('should return the page response correctly', () => {
      let result: PageResponse<SolicitudResumen> | undefined;
      service.listar(0, 10).subscribe(r => result = r);
      httpMock.expectOne(r => r.url === '/solicitudes').flush(mockPage);
      expect(result!.content.length).toBe(1);
      expect(result!.totalElements).toBe(1);
    });
  });

  describe('obtener()', () => {
    it('should GET /solicitudes/1', () => {
      service.obtener(1).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockDetalle);
    });

    it('should use the correct id in the URL', () => {
      service.obtener(42).subscribe();
      const req = httpMock.expectOne('/solicitudes/42');
      expect(req.request.method).toBe('GET');
      req.flush(mockDetalle);
    });
  });

  describe('registrar()', () => {
    it('should POST to /solicitudes', () => {
      const reqBody: RegistrarSolicitudRequest = {
        descripcion: 'Descripción larga de prueba para registrar una solicitud',
        canal: CanalOrigen.CSU,
        solicitanteId: '12345',
      };
      service.registrar(reqBody).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockDetalle);
    });
  });

  describe('clasificar()', () => {
    it('should POST to /solicitudes/1/clasificar', () => {
      const reqBody: ClasificarSolicitudRequest = {
        tipoSolicitudId: 2,
        nivelPrioridad: NivelPrioridad.ALTA,
        version: 0,
      };
      service.clasificar(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes/1/clasificar');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockDetalle);
    });
  });

  describe('asignar()', () => {
    it('should POST to /solicitudes/1/asignar', () => {
      const reqBody: AsignarResponsableRequest = {
        responsableId: 3,
        notas: 'Asignar a Juan',
        version: 1,
      };
      service.asignar(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes/1/asignar');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockDetalle);
    });
  });

  describe('marcarAtendida()', () => {
    it('should POST to /solicitudes/1/atender', () => {
      const reqBody: MarcarAtendidaRequest = {
        observacionResolucion: 'Se resolvió el problema',
        version: 2,
      };
      service.marcarAtendida(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes/1/atender');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockDetalle);
    });
  });

  describe('cerrar()', () => {
    it('should POST to /solicitudes/1/cerrar', () => {
      const reqBody: CerrarSolicitudRequest = {
        observacionCierre: 'Solicitud cerrada satisfactoriamente',
        version: 3,
      };
      service.cerrar(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes/1/cerrar');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockDetalle);
    });
  });

  describe('rechazar()', () => {
    it('should POST to /solicitudes/1/rechazar', () => {
      const reqBody: RechazarSolicitudRequest = {
        motivo: MotivoRechazo.DUPLICADA,
        justificacion: 'Ya existe una solicitud similar',
        version: 0,
      };
      service.rechazar(1, reqBody).subscribe(result => {
        expect(result).toEqual(mockDetalle);
      });
      const req = httpMock.expectOne('/solicitudes/1/rechazar');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reqBody);
      req.flush(mockDetalle);
    });
  });
});
