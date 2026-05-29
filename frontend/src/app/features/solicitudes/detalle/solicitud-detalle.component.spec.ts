import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { SolicitudDetalleComponent } from './solicitud-detalle.component';
import { AuthService } from '../../../core/services/auth.service';
import {
  EstadoSolicitud,
  NivelPrioridad,
  CanalOrigen,
  NombreRol,
  CategoriaSolicitud,
} from '../../../core/models';
import { SolicitudDetalleResponse } from '../../../core/models/solicitud.model';

describe('SolicitudDetalleComponent', () => {
  let component: SolicitudDetalleComponent;
  let fixture: ComponentFixture<SolicitudDetalleComponent>;
  let httpMock: HttpTestingController;

  const mockSolicitud: SolicitudDetalleResponse = {
    id: 1,
    codigo: 'SOL-001',
    descripcion: 'Descripción detallada de la solicitud de prueba',
    canal: CanalOrigen.CSU,
    estado: EstadoSolicitud.REGISTRADA,
    solicitante: { id: 10, nombre: 'Juan', apellido: 'Perez', identificacion: '123456', activo: true },
    contadorReaperturas: 0,
    fechaRegistro: '2024-01-15T10:00:00',
    fechaUltimaActualizacion: '2024-01-15T10:00:00',
    version: 0,
  };

  const mockSolicitudWithResponsable: SolicitudDetalleResponse = {
    ...mockSolicitud,
    responsableActual: { id: 20, nombre: 'Ana', apellido: 'Garcia', identificacion: '789', activo: true },
  };

  const mockSolicitudWithIA: SolicitudDetalleResponse = {
    ...mockSolicitud,
    sugerenciaIA: {
      id: 1,
      tipoSugerido: { id: 1, nombre: 'Tipo Test', tiempoAtencionDias: 5, activo: true, categoria: CategoriaSolicitud.DOCUMENTACION },
      prioridadSugerida: NivelPrioridad.ALTA,
      justificacionIA: 'Basado en el contenido del texto',
      confianza: 0.85,
      confirmada: false,
      ajustada: false,
      generadaEn: '2024-01-15T10:01:00',
    },
  };

  function createMockAuthService(roles: NombreRol[]) {
    return {
      currentUser: signal({ email: 'test@test.com', nombre: 'Test', apellido: 'User', roles, credentials: btoa('test@test.com:pass') }),
      isAuthenticated: () => true,
      hasRole: (r: NombreRol) => roles.includes(r),
      hasAnyRole: (...r: NombreRol[]) => r.some(role => roles.includes(role)),
      getCredentials: () => btoa('test@test.com:pass'),
    };
  }

  async function setupWithRoles(roles: NombreRol[]) {
    await TestBed.configureTestingModule({
      imports: [SolicitudDetalleComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: createMockAuthService(roles) },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '1' } } },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SolicitudDetalleComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  }

  beforeEach(async () => {
    await setupWithRoles([NombreRol.COORDINADOR]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    expect(component).toBeTruthy();
  });

  it('should show loading state initially', () => {
    fixture.detectChanges();
    expect(component.loading()).toBeTrue();
    const loadingEl = fixture.nativeElement.querySelector('.loading-state');
    expect(loadingEl).not.toBeNull();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
  });

  it('should render solicitud codigo after load', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const codeEl = fixture.nativeElement.querySelector('.code-display');
    expect(codeEl.textContent.trim()).toBe('SOL-001');
  }));

  it('should render solicitud descripcion after load', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const descEl = fixture.nativeElement.querySelector('.description-text');
    expect(descEl.textContent).toContain('Descripción detallada');
  }));

  it('should render canal info after load', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const metaValues = fixture.nativeElement.querySelectorAll('.meta-value');
    const texts = Array.from(metaValues).map((el: any) => el.textContent.trim());
    expect(texts.some(t => t.includes('CSU'))).toBeTrue();
  }));

  it('should show Clasificar button when REGISTRADA + COORDINADOR', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const texts = Array.from(buttons).map((b: any) => b.textContent.trim());
    expect(texts.some(t => t.includes('Clasificar'))).toBeTrue();
  }));

  it('should show Rechazar button when REGISTRADA + COORDINADOR', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const texts = Array.from(buttons).map((b: any) => b.textContent.trim());
    expect(texts.some(t => t.includes('Rechazar'))).toBeTrue();
  }));

  it('should show Asignar button when CLASIFICADA + COORDINADOR', fakeAsync(async () => {
    TestBed.resetTestingModule();
    await setupWithRoles([NombreRol.COORDINADOR]);
    const solicitudClasificada = { ...mockSolicitud, estado: EstadoSolicitud.CLASIFICADA };
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(solicitudClasificada);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const texts = Array.from(buttons).map((b: any) => b.textContent.trim());
    expect(texts.some(t => t.includes('Asignar'))).toBeTrue();
  }));

  it('should show Marcar Atendida button when EN_ATENCION + DOCENTE', fakeAsync(async () => {
    TestBed.resetTestingModule();
    await setupWithRoles([NombreRol.DOCENTE]);
    const solicitudEnAtencion = { ...mockSolicitud, estado: EstadoSolicitud.EN_ATENCION };
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(solicitudEnAtencion);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const texts = Array.from(buttons).map((b: any) => b.textContent.trim());
    expect(texts.some(t => t.includes('Marcar Atendida'))).toBeTrue();
  }));

  it('should show Cerrar button when ATENDIDA + ESTUDIANTE', fakeAsync(async () => {
    TestBed.resetTestingModule();
    await setupWithRoles([NombreRol.ESTUDIANTE]);
    const solicitudAtendida = { ...mockSolicitud, estado: EstadoSolicitud.ATENDIDA };
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(solicitudAtendida);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const texts = Array.from(buttons).map((b: any) => b.textContent.trim());
    expect(texts.some(t => t.includes('Cerrar'))).toBeTrue();
  }));

  it('should show no action buttons for CERRADA state', fakeAsync(async () => {
    TestBed.resetTestingModule();
    await setupWithRoles([NombreRol.COORDINADOR]);
    const solicitudCerrada = { ...mockSolicitud, estado: EstadoSolicitud.CERRADA };
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(solicitudCerrada);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    expect(buttons.length).toBe(0);
  }));

  it('should show sugerenciaIA card when sugerencia is present', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitudWithIA);
    tick();
    fixture.detectChanges();

    const iaCard = fixture.nativeElement.querySelector('.ia-card');
    expect(iaCard).not.toBeNull();
  }));

  it('should hide sugerenciaIA card when sugerencia is null', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const iaCard = fixture.nativeElement.querySelector('.ia-card');
    expect(iaCard).toBeNull();
  }));

  it('should show "Sin responsable asignado" when no responsable', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const noResponsable = fixture.nativeElement.querySelector('.no-responsable');
    expect(noResponsable).not.toBeNull();
    expect(noResponsable.textContent).toContain('Sin responsable asignado');
  }));

  it('should show responsable name when assigned', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitudWithResponsable);
    tick();
    fixture.detectChanges();

    const personItems = fixture.nativeElement.querySelectorAll('.person-name');
    const names = Array.from(personItems).map((el: any) => el.textContent.trim());
    expect(names.some(n => n.includes('Ana'))).toBeTrue();
  }));

  it('should open clasificar modal on Clasificar button click', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const clasificarBtn = Array.from(buttons).find((b: any) => b.textContent.includes('Clasificar')) as HTMLElement;
    clasificarBtn.click();
    tick();
    fixture.detectChanges();

    // Expect tipos-solicitud request when opening clasificar modal
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    fixture.detectChanges();

    expect(component.activeModal()).toBe('clasificar');
    const modal = fixture.nativeElement.querySelector('.modal');
    expect(modal).not.toBeNull();
    const modalTitle = fixture.nativeElement.querySelector('.modal-title');
    expect(modalTitle.textContent).toContain('Clasificar');
  }));

  it('should open asignar modal when CLASIFICADA and button clicked', fakeAsync(async () => {
    TestBed.resetTestingModule();
    await setupWithRoles([NombreRol.COORDINADOR]);
    const solicitudClasificada = { ...mockSolicitud, estado: EstadoSolicitud.CLASIFICADA };
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(solicitudClasificada);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.btn-action');
    const asignarBtn = Array.from(buttons).find((b: any) => b.textContent.includes('Asignar')) as HTMLElement;
    asignarBtn.click();
    tick();
    fixture.detectChanges();

    httpMock.expectOne(r => r.url === '/usuarios').flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 100, first: true, last: true });
    fixture.detectChanges();

    expect(component.activeModal()).toBe('asignar');
  }));

  it('should clear form fields when closeModal() is called', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/solicitudes/1').flush(mockSolicitud);
    tick();
    fixture.detectChanges();

    component.form.justificacionPrioridad = 'test justificacion';
    component.form.notas = 'test notas';
    component.form.observacionResolucion = 'test obs';
    component.form.observacionCierre = 'test cierre';
    component.form.justificacionRechazo = 'test rechazo';

    component.closeModal();

    expect(component.form.justificacionPrioridad).toBe('');
    expect(component.form.notas).toBe('');
    expect(component.form.observacionResolucion).toBe('');
    expect(component.form.observacionCierre).toBe('');
    expect(component.form.justificacionRechazo).toBe('');
    expect(component.activeModal()).toBeNull();
  }));
});
