import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ReglasPrioridadComponent } from './reglas-prioridad.component';
import { ReglaPrioridad, NivelPrioridad } from '../../../core/models';

describe('ReglasPrioridadComponent', () => {
  let component: ReglasPrioridadComponent;
  let fixture: ComponentFixture<ReglasPrioridadComponent>;
  let httpMock: HttpTestingController;

  const mockRegla: ReglaPrioridad = {
    id: 1,
    nombre: 'Regla Crítica',
    descripcion: 'Descripción de la regla',
    condicion: 'tipo == DOCUMENTACION && diasRestantes < 2',
    nivelResultante: NivelPrioridad.CRITICA,
    peso: 100,
    activa: true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglasPrioridadComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReglasPrioridadComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([]);
    expect(component).toBeTruthy();
  });

  it('should show loading state initially', () => {
    fixture.detectChanges();
    expect(component.loading()).toBeTrue();
    const loadingEl = fixture.nativeElement.querySelector('.loading-state');
    expect(loadingEl).not.toBeNull();
    httpMock.expectOne('/reglas-prioridad').flush([]);
  });

  it('should show empty state when no reglas', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([]);
    tick();
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).not.toBeNull();
  }));

  it('should show table when reglas are present', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
  }));

  it('should display condicion code in table', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
    fixture.detectChanges();

    const tableText = fixture.nativeElement.querySelector('tbody').textContent;
    expect(tableText).toContain('tipo == DOCUMENTACION && diasRestantes < 2');
  }));

  it('should open modal when "Nueva Regla" button is clicked', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
    fixture.detectChanges();

    const nuevoBtn = fixture.nativeElement.querySelector('button.btn-primary');
    nuevoBtn.click();
    fixture.detectChanges();

    expect(component.activeModal()).toBe('crear');
    const modal = fixture.nativeElement.querySelector('.modal');
    expect(modal).not.toBeNull();
  }));

  it('should show error when submitting with empty nombre', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([]);
    tick();
    fixture.detectChanges();

    component.openCrear();
    fixture.detectChanges();

    component.form.nombre = '';
    component.form.condicion = 'tipo == DOCUMENTACION';
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('obligatorios');
  }));

  it('should show error when submitting with empty condicion', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([]);
    tick();
    fixture.detectChanges();

    component.openCrear();
    component.form.nombre = 'Mi Regla';
    component.form.condicion = '';
    component.submit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
  }));

  it('should call service.crear() with valid data', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([]);
    tick();
    fixture.detectChanges();

    component.openCrear();
    component.form.nombre = 'Nueva Regla';
    component.form.condicion = 'tipo == DOCUMENTACION';
    component.form.nivelResultante = NivelPrioridad.ALTA;
    component.form.peso = 80;
    component.submit();

    const req = httpMock.expectOne('/reglas-prioridad');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.nombre).toBe('Nueva Regla');
    expect(req.request.body.condicion).toBe('tipo == DOCUMENTACION');
    req.flush(mockRegla);

    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
  }));

  it('should pre-fill form when opening edit modal', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
    fixture.detectChanges();

    component.openEditar(mockRegla);
    fixture.detectChanges();

    expect(component.activeModal()).toBe('editar');
    expect(component.form.nombre).toBe('Regla Crítica');
    expect(component.form.condicion).toBe('tipo == DOCUMENTACION && diasRestantes < 2');
    expect(component.form.nivelResultante).toBe(NivelPrioridad.CRITICA);
  }));

  it('should call service.actualizar() on edit submit', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
    fixture.detectChanges();

    component.openEditar(mockRegla);
    component.form.nombre = 'Regla Actualizada';
    component.submit();

    const req = httpMock.expectOne('/reglas-prioridad/1');
    expect(req.request.method).toBe('PUT');
    req.flush({ ...mockRegla, nombre: 'Regla Actualizada' });

    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
  }));

  it('should call service.eliminar() when confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();
    fixture.detectChanges();

    component.eliminar(mockRegla);

    const req = httpMock.expectOne('/reglas-prioridad/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne('/reglas-prioridad').flush([]);
    tick();
  }));

  it('should NOT call service.eliminar() when not confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    httpMock.expectOne('/reglas-prioridad').flush([mockRegla]);
    tick();

    component.eliminar(mockRegla);
    httpMock.expectNone('/reglas-prioridad/1');
  }));
});
