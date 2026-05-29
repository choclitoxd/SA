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

    // Try to hit /solicitudes (accessible by any authenticated user)
    this.http
      .get<any>('/solicitudes', { headers, params: { page: 0, size: 1 } })
      .subscribe({
        next: () => {
          // Now fetch the user from /usuarios filtered by email
          // Since there's no /me endpoint, we try /usuarios and search by email
          this.http
            .get<any>('/usuarios', { headers, params: { page: 0, size: 100 } })
            .subscribe({
              next: (resp) => {
                const users = resp.content ?? [];
                const me = users.find((u: any) => u.email === this.email);
                if (me) {
                  this.auth.setUser(this.email, this.password, {
                    nombre: me.nombre,
                    apellido: me.apellido,
                    roles: me.roles,
                  });
                } else {
                  // User is authenticated but not in ADMINISTRATIVO/DIRECTOR list
                  // Means they're ESTUDIANTE/DOCENTE — set minimal info
                  this.auth.setUser(this.email, this.password, {
                    nombre: this.email.split('@')[0],
                    apellido: '',
                    roles: [NombreRol.ESTUDIANTE],
                  });
                }
                this.router.navigate(['/dashboard']);
              },
              error: () => {
                // /usuarios returned 403 — user doesn't have admin access
                // Still let them in with minimal info
                this.auth.setUser(this.email, this.password, {
                  nombre: this.email.split('@')[0],
                  apellido: '',
                  roles: [NombreRol.ESTUDIANTE],
                });
                this.router.navigate(['/dashboard']);
              },
            });
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
