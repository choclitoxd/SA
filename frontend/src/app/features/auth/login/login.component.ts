import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { NombreRol } from '../../../core/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient);

  email = '';
  password = '';
  loading = signal(false);
  error = signal('');
  showPassword = signal(false);

  onSubmit() {
    if (!this.email || !this.password) {
      this.error.set('Completa todos los campos.');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    const credentials = btoa(`${this.email}:${this.password}`);
    const headers = { Authorization: `Basic ${credentials}` };

    // Usa /auth/me — accesible por cualquier rol autenticado
    this.http
      .get<any>('/auth/me', { headers })
      .subscribe({
        next: (me) => {
          this.auth.setUser(this.email, this.password, {
            nombre: me.nombre,
            apellido: me.apellido,
            roles: me.roles,
          });
          this.loading.set(false);
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.loading.set(false);
          if (err.status === 401) {
            this.error.set('Credenciales incorrectas. Verifica tu correo y contraseña.');
          } else if (err.status === 0) {
            this.error.set('No se puede conectar al servidor. Verifica que el backend esté corriendo en localhost:8080');
          } else {
            this.error.set(`Error al iniciar sesión (${err.status}).`);
          }
        },
      });
  }

  togglePassword() {
    this.showPassword.update(v => !v);
  }
}
