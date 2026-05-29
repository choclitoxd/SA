import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { TiposSolicitudComponent } from './tipos-solicitud.component';
import { TipoSolicitudResponse, CategoriaSolicitud } from '../../../core/models';

describe('TiposSolicitudComponent', () => {
  let component: TiposSolicitudComponent;
  let fixture: ComponentFixture<TiposSolicitudComponent>;
  let httpMock: HttpTestingController;

  const mockTipo: TipoSolicitudResponse = {
    id: 1,
    nombre: 'Solicitud de Matrícula',
    descripcion: 'Proceso de matrícula universitaria',
    tiempoAtencionDias: 5,
    activo: true,
    categoria: CategoriaSolicitud.GESTION_CURRICULAR,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TiposSolicitudComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TiposSolicitudComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    expect(component).toBeTruthy();
  });

  it('should show loading state initially', () => {
    fixture.detectChanges();
    expect(component.loading()).toBeTrue();
    const loadingEl = fixture.nativeElement.querySelector('.loading-state');
    expect(loadingEl).not.toBeNull();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
  });

  it('should show empty state when no tipos', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    tick();
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).not.toBeNull();
  }));

  it('should show table rows when tipos are present', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
  }));

  it('should display tipo nombre in table', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    const tableText = fixture.nativeElement.querySelector('tbody').textContent;
    expect(tableText).toContain('Solicitud de Matrícula');
  }));

  it('should display categoria in table', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    const tableText = fixture.nativeElement.querySelector('tbody').textContent;
    expect(tableText).toContain('Gestión Curricular');
  }));

  it('should open modal when "Nuevo Tipo" button is clicked', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('button.btn-primary');
    const nuevoBtn = buttons[0];
    nuevoBtn.click();
    fixture.detectChanges();

    expect(component.activeModal()).toBe('crear');
    const modal = fixture.nativeElement.querySelector('.modal');
    expect(modal).not.toBeNull();
  }));

  it('should show error when submitting with empty nombre', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    tick();
    fixture.detectChanges();

    component.openCrear();
    fixture.detectChanges();

    component.form.nombre = '';
    component.form.categoria = CategoriaSolicitud.GESTION_CURRICULAR;
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('obligatorios');
  }));

  it('should show error when submitting with empty categoria', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    tick();
    fixture.detectChanges();

    component.openCrear();
    component.form.nombre = 'Tipo Test';
    component.form.categoria = '';
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
  }));

  it('should call service.crear() with valid data', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    tick();
    fixture.detectChanges();

    component.openCrear();
    component.form.nombre = 'Nuevo Tipo';
    component.form.descripcion = 'Descripcion del tipo';
    component.form.tiempoAtencionDias = 3;
    component.form.categoria = CategoriaSolicitud.DOCUMENTACION;
    component.submit();

    const req = httpMock.expectOne('/tipos-solicitud');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.nombre).toBe('Nuevo Tipo');
    req.flush(mockTipo);

    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
  }));

  it('should open edit modal with pre-filled form values', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    component.openEditar(mockTipo);
    fixture.detectChanges();

    expect(component.activeModal()).toBe('editar');
    expect(component.form.nombre).toBe('Solicitud de Matrícula');
    expect(component.form.categoria).toBe(CategoriaSolicitud.GESTION_CURRICULAR);
  }));

  it('should call service.actualizar() on edit submit', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    component.openEditar(mockTipo);
    component.form.nombre = 'Tipo Actualizado';
    component.submit();

    const req = httpMock.expectOne('/tipos-solicitud/1');
    expect(req.request.method).toBe('PUT');
    req.flush(mockTipo);

    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
  }));

  it('should call service.desactivar() when confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();
    fixture.detectChanges();

    component.desactivar(mockTipo);

    const req = httpMock.expectOne('/tipos-solicitud/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([]);
    tick();
  }));

  it('should NOT call service.desactivar() when not confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/tipos-solicitud').flush([mockTipo]);
    tick();

    component.desactivar(mockTipo);
    httpMock.expectNone('/tipos-solicitud/1');
  }));
});
