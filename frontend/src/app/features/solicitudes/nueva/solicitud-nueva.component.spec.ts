import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Router } from '@angular/router';
import { SolicitudNuevaComponent } from './solicitud-nueva.component';
import { CanalOrigen, EstadoSolicitud } from '../../../core/models';
import { SolicitudDetalleResponse } from '../../../core/models/solicitud.model';

describe('SolicitudNuevaComponent', () => {
  let component: SolicitudNuevaComponent;
  let fixture: ComponentFixture<SolicitudNuevaComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockResponse: SolicitudDetalleResponse = {
    id: 42,
    codigo: 'SOL-042',
    descripcion: 'Descripción larga de al menos 30 caracteres para prueba',
    canal: CanalOrigen.CSU,
    estado: EstadoSolicitud.REGISTRADA,
    solicitante: { id: 1, nombre: 'Juan', apellido: 'Perez', identificacion: '123', activo: true },
    contadorReaperturas: 0,
    fechaRegistro: '2024-01-15T10:00:00',
    fechaUltimaActualizacion: '2024-01-15T10:00:00',
    version: 0,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SolicitudNuevaComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SolicitudNuevaComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render descripcion textarea', () => {
    const textarea = fixture.nativeElement.querySelector('textarea[name="descripcion"]');
    expect(textarea).not.toBeNull();
  });

  it('should render canal select', () => {
    const select = fixture.nativeElement.querySelector('select[name="canal"]');
    expect(select).not.toBeNull();
  });

  it('should render solicitanteId input', () => {
    const input = fixture.nativeElement.querySelector('input[name="solicitanteId"]');
    expect(input).not.toBeNull();
  });

  it('should show error when submitting with empty fields', () => {
    component.form.descripcion = '';
    component.form.canal = '';
    component.form.solicitanteId = '';
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('Completa todos los campos obligatorios');
  });

  it('should show error when descripcion is less than 30 characters', () => {
    component.form.descripcion = 'Corta';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('30 caracteres');
  });

  it('should show error when only descripcion is missing', () => {
    component.form.descripcion = '';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
  });

  it('should call service.registrar() on valid submit', fakeAsync(() => {
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.form.descripcion = 'Esta es una descripcion que tiene mas de treinta caracteres para la prueba';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();

    const req = httpMock.expectOne('/solicitudes');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.descripcion).toBe('Esta es una descripcion que tiene mas de treinta caracteres para la prueba');
    expect(req.request.body.canal).toBe(CanalOrigen.CSU);
    expect(req.request.body.solicitanteId).toBe('12345678');
    req.flush(mockResponse);
    tick();
  }));

  it('should navigate to /solicitudes/:id on success', fakeAsync(() => {
    const navigateSpy = spyOn(router, 'navigate');
    component.form.descripcion = 'Esta es una descripcion que tiene mas de treinta caracteres';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();

    httpMock.expectOne('/solicitudes').flush(mockResponse);
    tick();

    expect(navigateSpy).toHaveBeenCalledWith(['/solicitudes', 42]);
  }));

  it('should show error message on HTTP error', fakeAsync(() => {
    component.form.descripcion = 'Esta es una descripcion que tiene mas de treinta caracteres';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();

    httpMock.expectOne('/solicitudes').flush(
      { message: 'Error del servidor' },
      { status: 500, statusText: 'Internal Server Error' }
    );
    tick();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('Error del servidor');
  }));

  it('should show AI notice card', () => {
    const iaNotice = fixture.nativeElement.querySelector('.ia-notice');
    expect(iaNotice).not.toBeNull();
  });

  it('should set loading to true during submission', fakeAsync(() => {
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.form.descripcion = 'Esta es una descripcion que tiene mas de treinta caracteres';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();
    fixture.detectChanges();

    expect(component.loading()).toBeTrue();

    // Clean up
    httpMock.expectOne('/solicitudes').flush(mockResponse);
    tick();
  }));

  it('should reset loading to false on HTTP error', fakeAsync(() => {
    component.form.descripcion = 'Esta es una descripcion que tiene mas de treinta caracteres';
    component.form.canal = CanalOrigen.CSU;
    component.form.solicitanteId = '12345678';
    component.submit();

    httpMock.expectOne('/solicitudes').flush({}, { status: 400, statusText: 'Bad Request' });
    tick();

    expect(component.loading()).toBeFalse();
  }));
});
