import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReglasPrioridadService } from '../../../core/services/reglas-prioridad.service';
import { ReglaPrioridad, NivelPrioridad, PRIORIDAD_LABELS } from '../../../core/models';

@Component({
  selector: 'app-reglas-prioridad',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reglas-prioridad.component.html',
  styleUrl: './reglas-prioridad.component.scss',
})
export class ReglasPrioridadComponent implements OnInit {
  private service = inject(ReglasPrioridadService);

  loading = signal(true);
  submitting = signal(false);
  reglas = signal<ReglaPrioridad[]>([]);
  activeModal = signal<'crear' | 'editar' | null>(null);
  editTarget = signal<ReglaPrioridad | null>(null);
  error = signal('');

  prioridades = Object.values(NivelPrioridad);
  prioridadLabels = PRIORIDAD_LABELS;

  form: ReglaPrioridad = {
    nombre: '',
    descripcion: '',
    condicion: '',
    nivelResultante: NivelPrioridad.MEDIA,
    peso: 10,
    activa: true,
  };

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.service.listar().subscribe({
      next: (r) => { this.reglas.set(r); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openCrear() {
    this.form = { nombre: '', descripcion: '', condicion: '', nivelResultante: NivelPrioridad.MEDIA, peso: 10, activa: true };
    this.editTarget.set(null);
    this.error.set('');
    this.activeModal.set('crear');
  }

  openEditar(r: ReglaPrioridad) {
    this.form = { ...r };
    this.editTarget.set(r);
    this.error.set('');
    this.activeModal.set('editar');
  }

  closeModal() { this.activeModal.set(null); }

  submit() {
    if (!this.form.nombre || !this.form.condicion) { this.error.set('Nombre y condición son obligatorios.'); return; }
    this.submitting.set(true);
    const obs = this.editTarget()?.id
      ? this.service.actualizar(this.editTarget()!.id!, this.form)
      : this.service.crear(this.form);
    obs.subscribe({
      next: () => { this.closeModal(); this.load(); },
      error: (e) => { this.submitting.set(false); this.error.set(e?.error?.message ?? 'Error al guardar.'); },
    });
  }

  eliminar(r: ReglaPrioridad) {
    if (!confirm(`¿Eliminar la regla "${r.nombre}"?`)) return;
    this.service.eliminar(r.id!).subscribe({ next: () => this.load() });
  }

  getPrioridadClass(nivel: NivelPrioridad): string {
    const map: Record<string, string> = {
      CRITICA: 'prio--critica', ALTA: 'prio--alta', MEDIA: 'prio--media', BAJA: 'prio--baja',
    };
    return `prio-chip ${map[nivel] ?? ''}`;
  }
}
