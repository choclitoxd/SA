import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { UsuariosComponent } from './usuarios.component';
import { NombreRol, UsuarioResponse } from '../../core/models';
import { PageResponse } from '../../core/models/usuario.model';

describe('UsuariosComponent', () => {
  let component: UsuariosComponent;
  let fixture: ComponentFixture<UsuariosComponent>;
  let httpMock: HttpTestingController;

  const mockUsuario: UsuarioResponse = {
    id: 1,
    nombre: 'Maria',
    apellido: 'Garcia',
    email: 'maria@test.com',
    identificacion: '987654',
    roles: [NombreRol.COORDINADOR],
    activo: true,
    creadoEn: '2024-01-01T00:00:00',
  };

  const mockPage: PageResponse<UsuarioResponse> = {
    content: [mockUsuario],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 15,
    first: true,
    last: true,
  };

  const emptyPage: PageResponse<UsuarioResponse> = {
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 15,
    first: true,
    last: true,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UsuariosComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    expect(component).toBeTruthy();
  });

  it('should show loading state initially', () => {
    fixture.detectChanges();
    expect(component.loading()).toBeTrue();
    const loadingEl = fixture.nativeElement.querySelector('.loading-state');
    expect(loadingEl).not.toBeNull();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
  });

  it('should show users table after load', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
  }));

  it('should show empty state when no users', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(emptyPage);
    tick();
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).not.toBeNull();
  }));

  it('should open create modal when "Nuevo Usuario" button is clicked', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    const nuevoBtn = fixture.nativeElement.querySelector('button.btn-primary');
    nuevoBtn.click();
    fixture.detectChanges();

    expect(component.activeModal()).toBe('crear');
    const modal = fixture.nativeElement.querySelector('.modal');
    expect(modal).not.toBeNull();
  }));

  it('should open edit modal with pre-filled data when edit button is clicked', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    const editBtn = fixture.nativeElement.querySelector('.btn-icon');
    editBtn.click();
    fixture.detectChanges();

    expect(component.activeModal()).toBe('editar');
    expect(component.form.nombre).toBe('Maria');
    expect(component.form.apellido).toBe('Garcia');
    expect(component.form.email).toBe('maria@test.com');
    expect(component.form.roles).toContain(NombreRol.COORDINADOR);
  }));

  it('should add role when toggleRole() is called with a new role', () => {
    component.form.roles = [];
    component.toggleRole(NombreRol.ESTUDIANTE);
    expect(component.form.roles).toContain(NombreRol.ESTUDIANTE);
  });

  it('should remove role when toggleRole() is called with an existing role', () => {
    component.form.roles = [NombreRol.ESTUDIANTE];
    component.toggleRole(NombreRol.ESTUDIANTE);
    expect(component.form.roles).not.toContain(NombreRol.ESTUDIANTE);
  });

  it('should keep other roles when toggleRole() removes one', () => {
    component.form.roles = [NombreRol.ESTUDIANTE, NombreRol.COORDINADOR];
    component.toggleRole(NombreRol.ESTUDIANTE);
    expect(component.form.roles).not.toContain(NombreRol.ESTUDIANTE);
    expect(component.form.roles).toContain(NombreRol.COORDINADOR);
  });

  it('hasRole() should return true for an assigned role', () => {
    component.form.roles = [NombreRol.COORDINADOR];
    expect(component.hasRole(NombreRol.COORDINADOR)).toBeTrue();
  });

  it('hasRole() should return false for an unassigned role', () => {
    component.form.roles = [NombreRol.COORDINADOR];
    expect(component.hasRole(NombreRol.ESTUDIANTE)).toBeFalse();
  });

  it('should show error when submitting crear with no roles', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    component.openCrear();
    fixture.detectChanges();

    component.form.roles = [];
    component.submitCrear();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('rol');
  }));

  it('should call service.crear() with valid data', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    component.openCrear();
    component.form.nombre = 'Carlos';
    component.form.apellido = 'Lopez';
    component.form.email = 'carlos@test.com';
    component.form.identificacion = '111222';
    component.form.password = 'Password1!';
    component.form.roles = [NombreRol.DOCENTE];
    component.submitCrear();

    const req = httpMock.expectOne('/usuarios');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.nombre).toBe('Carlos');
    req.flush(mockUsuario);

    // Reload call
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
  }));

  it('should call service.actualizar() on submitEditar()', fakeAsync(() => {
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    component.openEditar(mockUsuario);
    component.form.nombre = 'Maria Updated';
    component.submitEditar();

    const req = httpMock.expectOne('/usuarios/1');
    expect(req.request.method).toBe('PUT');
    req.flush(mockUsuario);

    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
  }));

  it('should call service.desactivar() when confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    component.desactivar(mockUsuario);

    const req = httpMock.expectOne('/usuarios/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
  }));

  it('should NOT call service.desactivar() when not confirmed', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(false);
    fixture.detectChanges();
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockPage);
    tick();
    fixture.detectChanges();

    component.desactivar(mockUsuario);
    httpMock.expectNone('/usuarios/1');
  }));
});
