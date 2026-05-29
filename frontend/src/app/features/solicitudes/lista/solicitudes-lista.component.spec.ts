import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { SolicitudesListaComponent } from './solicitudes-lista.component';
import { AuthService } from '../../../core/services/auth.service';
import { EstadoSolicitud, NivelPrioridad, NombreRol } from '../../../core/models';
import { SolicitudResumen } from '../../../core/models/solicitud.model';
import { PageResponse } from '../../../core/models/usuario.model';

describe('SolicitudesListaComponent', () => {
  let component: SolicitudesListaComponent;
  let fixture: ComponentFixture<SolicitudesListaComponent>;
  let httpMock: HttpTestingController;

  const mockSolicitud: SolicitudResumen = {
    id: 1,
    codigo: 'SOL-001',
    estado: EstadoSolicitud.REGISTRADA,
    prioridad: NivelPrioridad.ALTA,
    solicitanteNombre: 'Juan Perez',
    fechaRegistro: '2024-01-15T10:00:00',
    vencida: false,
  };

  const mockVencidaSolicitud: SolicitudResumen = {
    id: 2,
    codigo: 'SOL-002',
    estado: EstadoSolicitud.EN_ATENCION,
    solicitanteNombre: 'Maria Lopez',
    fechaRegistro: '2024-01-14T10:00:00',
    vencida: true,
  };

  const buildPage = (
    items: SolicitudResumen[],
    totalPages = 1,
    total = items.length
  ): PageResponse<SolicitudResumen> => ({
    content: items,
    totalElements: total,
    totalPages,
    number: 0,
    size: 10,
    first: true,
    last: totalPages === 1,
  });

  function createMockAuthService(roles: NombreRol[]) {
    return {
      currentUser: signal({ email: 'test@test.com', nombre: 'Test', apellido: 'User', roles, credentials: btoa('test@test.com:pass') }),
      isAuthenticated: () => true,
      hasRole: (r: NombreRol) => roles.includes(r),
      hasAnyRole: (...r: NombreRol[]) => r.some(role => roles.includes(role)),
      getCredentials: () => btoa('test@test.com:pass'),
    };
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SolicitudesListaComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: createMockAuthService([NombreRol.COORDINADOR]) },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SolicitudesListaComponent);
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

  it('should show loading state initially', () => {
    fixture.detectChanges();
    expect(component.loading()).toBeTrue();
    const loadingEl = fixture.nativeElement.querySelector('.loading-state');
    expect(loadingEl).not.toBeNull();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
  });

  it('should show empty-state element when response is empty', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    tick();
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).not.toBeNull();
  }));

  it('should show table with rows when solicitudes are present', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud, mockVencidaSolicitud]));
    tick();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
  }));

  it('should hide pagination controls when totalPages <= 1', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud], 1));
    tick();
    fixture.detectChanges();

    const pagination = fixture.nativeElement.querySelector('.pagination');
    expect(pagination).toBeNull();
  }));

  it('should show pagination when totalPages > 1', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud], 3, 30));
    tick();
    fixture.detectChanges();

    const pagination = fixture.nativeElement.querySelector('.pagination');
    expect(pagination).not.toBeNull();
  }));

  it('should call service.listar with next page when next button is clicked', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud], 3, 30));
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.page-btn');
    const nextBtn = buttons[buttons.length - 1];
    nextBtn.click();
    tick();

    const nextReq = httpMock.expectOne(r => r.url === '/solicitudes');
    expect(nextReq.request.params.get('page')).toBe('1');
    nextReq.flush(buildPage([mockSolicitud], 3, 30));
    tick();
  }));

  it('should show "Nueva Solicitud" button for ESTUDIANTE role', fakeAsync(() => {
    // Auth service mock (set up in beforeEach) has COORDINADOR role, which canCreate checks include
    // Test that an ESTUDIANTE auth service also grants canCreate
    const estudianteMock = createMockAuthService([NombreRol.ESTUDIANTE]);
    component['auth'] = estudianteMock as any;

    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    tick();
    fixture.detectChanges();

    // canCreate is true for ESTUDIANTE
    expect(component.canCreate).toBeTrue();
  }));

  it('should show "Nueva Solicitud" button for DOCENTE role', fakeAsync(() => {
    const docenteMock = createMockAuthService([NombreRol.DOCENTE]);
    component['auth'] = docenteMock as any;

    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([]));
    tick();
    fixture.detectChanges();

    // canCreate is true for DOCENTE
    expect(component.canCreate).toBeTrue();
  }));

  it('should display solicitud codigo in table', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const codeEl = fixture.nativeElement.querySelector('.code');
    expect(codeEl.textContent.trim()).toContain('SOL-001');
  }));

  it('should show estado badge in rows', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const badgeEl = fixture.nativeElement.querySelector('.badge');
    expect(badgeEl).not.toBeNull();
  }));

  it('should show prioridad badge when prioridad is set', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud]));
    tick();
    fixture.detectChanges();

    const prioBadge = fixture.nativeElement.querySelector('.prio-badge');
    expect(prioBadge).not.toBeNull();
  }));

  it('should apply row--vencida class to vencida solicitudes', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockSolicitud, mockVencidaSolicitud]));
    tick();
    fixture.detectChanges();

    const vencidaRows = fixture.nativeElement.querySelectorAll('.row--vencida');
    expect(vencidaRows.length).toBe(1);
  }));

  it('should show VEN tag for vencida solicitud', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/solicitudes').flush(buildPage([mockVencidaSolicitud]));
    tick();
    fixture.detectChanges();

    const venTag = fixture.nativeElement.querySelector('.tag-vencida');
    expect(venTag).not.toBeNull();
    expect(venTag.textContent.trim()).toBe('VEN');
  }));
});
