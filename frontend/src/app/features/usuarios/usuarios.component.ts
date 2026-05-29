import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuariosService } from '../../core/services/usuarios.service';
import {
  UsuarioResponse, NombreRol, CrearUsuarioRequest, ActualizarUsuarioRequest,
} from '../../core/models';

type ModalType = 'crear' | 'editar' | null;

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss',
})
export class UsuariosComponent implements OnInit {
  private service = inject(UsuariosService);

  loading = signal(true);
  submitting = signal(false);
  usuarios = signal<UsuarioResponse[]>([]);
  totalElements = signal(0);
  currentPage = signal(0);
  totalPages = signal(0);
  activeModal = signal<ModalType>(null);
  editTarget = signal<UsuarioResponse | null>(null);
  error = signal('');

  roles = Object.values(NombreRol);

  form = {
    nombre: '',
    apellido: '',
    email: '',
    identificacion: '',
    password: '',
    roles: [] as NombreRol[],
  };

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.service.listar(this.currentPage(), 15).subscribe({
      next: (p) => {
        this.usuarios.set(p.content);
        this.totalElements.set(p.totalElements);
        this.totalPages.set(p.totalPages);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openCrear() {
    this.error.set('');
    this.form = { nombre: '', apellido: '', email: '', identificacion: '', password: '', roles: [] };
    this.editTarget.set(null);
    this.activeModal.set('crear');
  }

  openEditar(u: UsuarioResponse) {
    this.error.set('');
    this.form = { nombre: u.nombre, apellido: u.apellido, email: u.email, identificacion: u.identificacion, password: '', roles: [...u.roles] };
    this.editTarget.set(u);
    this.activeModal.set('editar');
  }

  closeModal() { this.activeModal.set(null); this.editTarget.set(null); }

  toggleRole(role: NombreRol) {
    const idx = this.form.roles.indexOf(role);
    if (idx >= 0) this.form.roles.splice(idx, 1);
    else this.form.roles.push(role);
  }

  hasRole(role: NombreRol): boolean { return this.form.roles.includes(role); }

  submitCrear() {
    if (this.form.roles.length === 0) { this.error.set('Selecciona al menos un rol.'); return; }
    this.submitting.set(true);
    const req: CrearUsuarioRequest = {
      nombre: this.form.nombre, apellido: this.form.apellido,
      email: this.form.email, identificacion: this.form.identificacion,
      password: this.form.password, roles: this.form.roles,
    };
    this.service.crear(req).subscribe({
      next: () => { this.closeModal(); this.load(); },
      error: (e) => { this.submitting.set(false); this.error.set(e?.error?.message ?? 'Error al crear usuario.'); },
    });
  }

  submitEditar() {
    const id = this.editTarget()!.id;
    this.submitting.set(true);
    const req: ActualizarUsuarioRequest = {
      nombre: this.form.nombre, apellido: this.form.apellido,
      email: this.form.email, roles: this.form.roles.length ? this.form.roles : undefined,
    };
    this.service.actualizar(id, req).subscribe({
      next: () => { this.closeModal(); this.load(); },
      error: (e) => { this.submitting.set(false); this.error.set(e?.error?.message ?? 'Error al actualizar.'); },
    });
  }

  desactivar(u: UsuarioResponse) {
    if (!confirm(`¿Desactivar a ${u.nombre} ${u.apellido}?`)) return;
    this.service.desactivar(u.id).subscribe({ next: () => this.load() });
  }

  goToPage(p: number) {
    if (p < 0 || p >= this.totalPages()) return;
    this.currentPage.set(p);
    this.load();
  }

  get pages(): number[] { return Array.from({ length: this.totalPages() }, (_, i) => i); }
}
