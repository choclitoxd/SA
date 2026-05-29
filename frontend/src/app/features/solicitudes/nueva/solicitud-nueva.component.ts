import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SolicitudesService } from '../../../core/services/solicitudes.service';
import { CanalOrigen, CANAL_LABELS } from '../../../core/models';

@Component({
  selector: 'app-solicitud-nueva',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './solicitud-nueva.component.html',
  styleUrl: './solicitud-nueva.component.scss',
})
export class SolicitudNuevaComponent {
  private service = inject(SolicitudesService);
  private router = inject(Router);

  canales = Object.values(CanalOrigen);
  canalLabels = CANAL_LABELS;

  form = {
    descripcion: '',
    canal: '' as CanalOrigen | '',
    solicitanteId: '',
    fechaLimite: '',
  };

  loading = signal(false);
  error = signal('');

  submit() {
    if (!this.form.descripcion || !this.form.canal || !this.form.solicitanteId) {
      this.error.set('Completa todos los campos obligatorios.');
      return;
    }
    if (this.form.descripcion.length < 30) {
      this.error.set('La descripción debe tener al menos 30 caracteres.');
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.service.registrar({
      descripcion: this.form.descripcion,
      canal: this.form.canal as CanalOrigen,
      solicitanteId: this.form.solicitanteId,
      fechaLimite: this.form.fechaLimite || undefined,
    }).subscribe({
      next: (s) => this.router.navigate(['/solicitudes', s.id]),
      error: (e) => {
        this.loading.set(false);
        this.error.set(e?.error?.message ?? 'Error al registrar la solicitud.');
      },
    });
  }
}
