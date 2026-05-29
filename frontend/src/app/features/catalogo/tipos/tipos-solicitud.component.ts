import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TiposSolicitudService } from '../../../core/services/tipos-solicitud.service';
import { TipoSolicitudResponse, CategoriaSolicitud, CATEGORIA_LABELS } from '../../../core/models';

@Component({
  selector: 'app-tipos-solicitud',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tipos-solicitud.component.html',
  styleUrl: './tipos-solicitud.component.scss',
})
export class TiposSolicitudComponent implements OnInit {
  private service = inject(TiposSolicitudService);

  loading = signal(true);
  submitting = signal(false);
  tipos = signal<TipoSolicitudResponse[]>([]);
  activeModal = signal<'crear' | 'editar' | null>(null);
  editTarget = signal<TipoSolicitudResponse | null>(null);
  error = signal('');

  categorias = Object.values(CategoriaSolicitud);
  categoriaLabels = CATEGORIA_LABELS;

  form = {
    nombre: '',
    descripcion: '',
    tiempoAtencionDias: 5,
    categoria: '' as CategoriaSolicitud | '',
  };

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.service.listar().subscribe({
      next: (t) => { this.tipos.set(t); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openCrear() {
    this.form = { nombre: '', descripcion: '', tiempoAtencionDias: 5, categoria: '' };
    this.editTarget.set(null);
    this.error.set('');
    this.activeModal.set('crear');
  }

  openEditar(t: TipoSolicitudResponse) {
    this.form = { nombre: t.nombre, descripcion: t.descripcion ?? '', tiempoAtencionDias: t.tiempoAtencionDias, categoria: t.categoria };
    this.editTarget.set(t);
    this.error.set('');
    this.activeModal.set('editar');
  }

  closeModal() { this.activeModal.set(null); }

  submit() {
    if (!this.form.nombre || !this.form.categoria) { this.error.set('Nombre y categoría son obligatorios.'); return; }
    this.submitting.set(true);
    const req = { nombre: this.form.nombre, descripcion: this.form.descripcion || undefined, tiempoAtencionDias: this.form.tiempoAtencionDias, categoria: this.form.categoria as CategoriaSolicitud };
    const obs = this.editTarget()
      ? this.service.actualizar(this.editTarget()!.id, req)
      : this.service.crear(req);
    obs.subscribe({
      next: () => { this.closeModal(); this.load(); },
      error: (e) => { this.submitting.set(false); this.error.set(e?.error?.message ?? 'Error al guardar.'); },
    });
  }

  desactivar(t: TipoSolicitudResponse) {
    if (!confirm(`¿Desactivar "${t.nombre}"?`)) return;
    this.service.desactivar(t.id).subscribe({ next: () => this.load() });
  }
}
