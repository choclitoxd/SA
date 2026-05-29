import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SolicitudesService } from '../../core/services/solicitudes.service';
import { AuthService } from '../../core/services/auth.service';
import { SolicitudResumen, EstadoSolicitud, NivelPrioridad, ESTADO_LABELS, PRIORIDAD_LABELS } from '../../core/models';

interface StatCard {
  label: string;
  value: number;
  color: string;
  icon: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private solicitudesService = inject(SolicitudesService);
  auth = inject(AuthService);

  loading = signal(true);
  recientes = signal<SolicitudResumen[]>([]);
  totalElements = signal(0);

  estadoLabels = ESTADO_LABELS;
  prioridadLabels = PRIORIDAD_LABELS;

  stats = signal<StatCard[]>([]);

  ngOnInit() {
    this.solicitudesService.listar(0, 50).subscribe({
      next: (page) => {
        const items = page.content;
        this.totalElements.set(page.totalElements);
        this.recientes.set(items.slice(0, 8));
        this.buildStats(items, page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private buildStats(items: SolicitudResumen[], total: number) {
    const registradas = items.filter(s => s.estado === EstadoSolicitud.REGISTRADA).length;
    const enAtencion = items.filter(s => s.estado === EstadoSolicitud.EN_ATENCION).length;
    const vencidas = items.filter(s => s.vencida).length;
    const criticas = items.filter(s => s.prioridad === NivelPrioridad.CRITICA).length;

    this.stats.set([
      { label: 'Total Solicitudes', value: total, color: 'blue', icon: 'inbox' },
      { label: 'Pendientes', value: registradas, color: 'amber', icon: 'clock' },
      { label: 'En Atención', value: enAtencion, color: 'indigo', icon: 'activity' },
      { label: 'Críticas', value: criticas, color: 'red', icon: 'alert' },
    ]);
  }

  getEstadoClass(estado: EstadoSolicitud): string {
    const map: Record<string, string> = {
      REGISTRADA: 'badge--registrada',
      CLASIFICADA: 'badge--clasificada',
      EN_ATENCION: 'badge--en-atencion',
      ATENDIDA: 'badge--atendida',
      CERRADA: 'badge--cerrada',
      RECHAZADA: 'badge--rechazada',
      CANCELADA: 'badge--cancelada',
      VENCIDA: 'badge--vencida',
      ESCALADA: 'badge--escalada',
      IMPUGNADA: 'badge--impugnada',
    };
    return `badge ${map[estado] ?? ''}`;
  }

  getPrioridadClass(nivel?: NivelPrioridad): string {
    if (!nivel) return 'prio-badge';
    const map: Record<string, string> = {
      CRITICA: 'prio-badge--critica',
      ALTA: 'prio-badge--alta',
      MEDIA: 'prio-badge--media',
      BAJA: 'prio-badge--baja',
    };
    return `prio-badge ${map[nivel] ?? ''}`;
  }

  get greeting(): string {
    const h = new Date().getHours();
    const user = this.auth.currentUser();
    const name = user?.nombre ?? 'Usuario';
    if (h < 12) return `Buenos días, ${name}`;
    if (h < 18) return `Buenas tardes, ${name}`;
    return `Buenas noches, ${name}`;
  }

  getStatIcon(name: string): string {
    const icons: Record<string, string> = {
      inbox: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 002 2h16a2 2 0 002-2v-6l-3.45-6.89A2 2 0 0016.76 4H7.24a2 2 0 00-1.79 1.11z"/></svg>',
      clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
      activity: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>',
      alert: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>',
    };
    return icons[name] ?? '';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }
}
