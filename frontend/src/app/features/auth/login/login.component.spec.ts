import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Router } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { NombreRol } from '../../../core/models';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockSolicitudesResponse = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 1, first: true, last: true };
  const mockUsersResponse = {
    content: [{ id: 1, nombre: 'Admin', apellido: 'User', email: 'admin@test.com', identificacion: '123', roles: [NombreRol.COORDINADOR], activo: true, creadoEn: '2024-01-01' }],
    totalElements: 1, totalPages: 1, number: 0, size: 100, first: true, last: true,
  };

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        AuthService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render email input', () => {
    const emailInput = fixture.nativeElement.querySelector('input[type="email"]');
    expect(emailInput).not.toBeNull();
  });

  it('should render password input', () => {
    const passwordInput = fixture.nativeElement.querySelector('input[id="password"]');
    expect(passwordInput).not.toBeNull();
  });

  it('should show error when submitting with empty fields', () => {
    component.email = '';
    component.password = '';
    component.onSubmit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
    expect(errorEl.textContent).toContain('Completa todos los campos');
  });

  it('should show error when only email is provided', () => {
    component.email = 'test@test.com';
    component.password = '';
    component.onSubmit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
  });

  it('should show error when only password is provided', () => {
    component.email = '';
    component.password = 'password123';
    component.onSubmit();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl).not.toBeNull();
  });

  it('should toggle password visibility when toggle button is clicked', () => {
    const toggleBtn = fixture.nativeElement.querySelector('.toggle-pw');
    expect(toggleBtn).not.toBeNull();

    expect(component.showPassword()).toBeFalse();
    toggleBtn.click();
    fixture.detectChanges();
    expect(component.showPassword()).toBeTrue();

    toggleBtn.click();
    fixture.detectChanges();
    expect(component.showPassword()).toBeFalse();
  });

  it('should change password input type when toggle is clicked', () => {
    const toggleBtn = fixture.nativeElement.querySelector('.toggle-pw');
    const pwInput = fixture.nativeElement.querySelector('#password');

    expect(pwInput.type).toBe('password');
    toggleBtn.click();
    fixture.detectChanges();
    expect(pwInput.type).toBe('text');
  });

  it('should call HTTP on submit with valid credentials', fakeAsync(() => {
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.email = 'admin@test.com';
    component.password = 'password123';
    component.onSubmit();

    const solicitudesReq = httpMock.expectOne(r =>
      r.url === '/solicitudes' && r.headers.has('Authorization')
    );
    expect(solicitudesReq.request.method).toBe('GET');
    solicitudesReq.flush(mockSolicitudesResponse);

    const usersReq = httpMock.expectOne(r =>
      r.url === '/usuarios' && r.headers.has('Authorization')
    );
    usersReq.flush(mockUsersResponse);
    tick();
  }));

  it('should show "Credenciales incorrectas" message on HTTP 401', fakeAsync(() => {
    component.email = 'bad@test.com';
    component.password = 'wrongpass';
    component.onSubmit();

    const req = httpMock.expectOne(r => r.url === '/solicitudes');
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
    tick();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl.textContent).toContain('Credenciales incorrectas');
  }));

  it('should show "No se puede conectar al servidor" on HTTP status 0', fakeAsync(() => {
    component.email = 'test@test.com';
    component.password = 'pass';
    component.onSubmit();

    const req = httpMock.expectOne(r => r.url === '/solicitudes');
    req.flush(null, { status: 0, statusText: 'Unknown Error' });
    tick();
    fixture.detectChanges();

    const errorEl = fixture.nativeElement.querySelector('.error-alert');
    expect(errorEl.textContent).toContain('No se puede conectar al servidor');
  }));

  it('should show loading spinner during submit', fakeAsync(() => {
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    component.email = 'admin@test.com';
    component.password = 'password123';
    component.onSubmit();
    fixture.detectChanges();

    const spinner = fixture.nativeElement.querySelector('.spinner');
    expect(spinner).not.toBeNull();

    // Clean up pending requests
    httpMock.expectOne(r => r.url === '/solicitudes').flush(mockSolicitudesResponse);
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockUsersResponse);
    tick();
  }));

  it('should hide loading spinner after error response', fakeAsync(() => {
    component.email = 'test@test.com';
    component.password = 'pass';
    component.onSubmit();

    const req = httpMock.expectOne(r => r.url === '/solicitudes');
    req.flush(null, { status: 401, statusText: 'Unauthorized' });
    tick();
    fixture.detectChanges();

    expect(component.loading()).toBeFalse();
    const submitBtn = fixture.nativeElement.querySelector('.submit-btn');
    expect(submitBtn.disabled).toBeFalse();
  }));

  it('should navigate to /dashboard after successful login', fakeAsync(() => {
    const navigateSpy = spyOn(router, 'navigate');
    component.email = 'admin@test.com';
    component.password = 'password123';
    component.onSubmit();

    httpMock.expectOne(r => r.url === '/solicitudes').flush(mockSolicitudesResponse);
    httpMock.expectOne(r => r.url === '/usuarios').flush(mockUsersResponse);
    tick();

    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  }));
});
