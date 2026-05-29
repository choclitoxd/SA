import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../core/services/auth.service';
import { SolicitudesService } from '../../core/services/solicitudes.service';
import { EstadoSolicitud, NivelPrioridad, CanalOrigen, NombreRol } from '../../core/models';
import { SolicitudResumen } from '../../core/models/solicitud.model';
import { PageResponse } from '../../core/models/usuario.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let httpMock: HttpTestingController;

  const mockUser = {
    email: 'test@test.com',
    nombre: 'Test',
    apellido: 'User',
    roles: [NombreRol.COORDINADOR],
    credentials: btoa('test@test.com:pass'),
  };

  const mockSolicitud: SolicitudResumen = {
    id: 1,
    codigo: 'SOL-001',
    estado: EstadoSolicitud.REGISTRADA,
    solicitanteNombre: 'Juan Perez',
    fechaRegistro: '2024-01-15T10:00:00',
    vencida: false,
  };

  const mockVencidaSolicitud: SolicitudResumen = {
    id: 2,
    codigo: 'SOL-002',
    estado: EstadoSolicitud.EN_ATENCION,
    prioridad: NivelPrioridad.CRITICA,
    solicitanteNombre: 'Maria Lopez',
    fechaRegistro: '2024-01-14T10:00:00',
    vencida: true,
  };

  const buildPage = (items: SolicitudResumen[], total = items.length): PageResponse<SolicitudResumen> => ({
    content: items,
    totalElements: total,
    totalPages: 1,
    number: 0,
    size: 50,
    first: true,
    last: true,
  });

  const mockAuthService = {
    currentUser: signal(mockUser),
    isAuthenticated: () => true,
    hasRole: () => false,
    hasAnyRole: () => false,
    getCredentials: () => mockUser.credentials,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    expect(component).toBeTruthy();
  });

  it('should show loading spinner initially before HTTP completes', () => {
    fixture.detectChanges();
    expect(component.loading()).toBeTrue();
    const spinner = fixture.nativeElement.querySelector('.spinner-lg');
    expect(spinner).not.toBeNull();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
  });

  it('should hide loading spinner after HTTP response', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    tick();
    fixture.detectChanges();
    expect(component.loading()).toBeFalse();
    const spinner = fixture.nativeElement.querySelector('.spinner-lg');
    expect(spinner).toBeNull();
  }));

  it('should show stats grid with 4 cards after load', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const statCards = fixture.nativeElement.querySelectorAll('.stat-card');
    expect(statCards.length).toBe(4);
  }));

  it('should show greeting with "Bienvenido" type text', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    tick();
    fixture.detectChanges();

    const greetingEl = fixture.nativeElement.querySelector('.greeting');
    expect(greetingEl).not.toBeNull();
    expect(greetingEl.textContent).toContain('Test');
  }));

  it('should show empty state when no solicitudes', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    tick();
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).not.toBeNull();
  }));

  it('should show table rows when solicitudes are present', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud, mockVencidaSolicitud]));
    tick();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
  }));

  it('should apply stat-card--blue class to Total Solicitudes card', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud], 5));
    tick();
    fixture.detectChanges();

    const blueCard = fixture.nativeElement.querySelector('.stat-card--blue');
    expect(blueCard).not.toBeNull();
  }));

  it('should apply stat-card--amber class to Pendientes card', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const amberCard = fixture.nativeElement.querySelector('.stat-card--amber');
    expect(amberCard).not.toBeNull();
  }));

  it('should apply stat-card--indigo class to En Atencion card', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const indigoCard = fixture.nativeElement.querySelector('.stat-card--indigo');
    expect(indigoCard).not.toBeNull();
  }));

  it('should apply stat-card--red class to Criticas card', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const redCard = fixture.nativeElement.querySelector('.stat-card--red');
    expect(redCard).not.toBeNull();
  }));

  it('should apply row--vencida class to vencida rows', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud, mockVencidaSolicitud]));
    tick();
    fixture.detectChanges();

    const vencidaRows = fixture.nativeElement.querySelectorAll('.row--vencida');
    expect(vencidaRows.length).toBe(1);
  }));

  it('should display solicitud codigo in table', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const codeEl = fixture.nativeElement.querySelector('.code');
    expect(codeEl.textContent.trim()).toBe('SOL-001');
  }));

  it('should show VENCIDA tag for expired solicitudes', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockVencidaSolicitud]));
    tick();
    fixture.detectChanges();

    const vencidaTag = fixture.nativeElement.querySelector('.vencida-tag');
    expect(vencidaTag).not.toBeNull();
    expect(vencidaTag.textContent.trim()).toBe('VENCIDA');
  }));
});
