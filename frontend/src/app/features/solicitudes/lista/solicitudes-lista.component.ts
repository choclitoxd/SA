import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SolicitudesService } from '../../../core/services/solicitudes.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  SolicitudResumen,
  EstadoSolicitud,
  NivelPrioridad,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  NombreRol,
} from '../../../core/models';

@Component({
  selector: 'app-solicitudes-lista',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './solicitudes-lista.component.html',
  styleUrl: './solicitudes-lista.component.scss',
})
export class SolicitudesListaComponent implements OnInit {
  private service = inject(SolicitudesService);
  auth = inject(AuthService);

  loading = signal(true);
  solicitudes = signal<SolicitudResumen[]>([]);
  totalPages = signal(0);
  totalElements = signal(0);
  currentPage = signal(0);
  pageSize = 10;

  estadoLabels = ESTADO_LABELS;
  prioridadLabels = PRIORIDAD_LABELS;
  estados = Object.values(EstadoSolicitud);
  prioridades = Object.values(NivelPrioridad);

  NombreRol = NombreRol;

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.service.listar(this.currentPage(), this.pageSize).subscribe({
      next: (page) => {
        this.solicitudes.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages()) return;
    this.currentPage.set(page);
    this.load();
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  }

  getEstadoClass(estado: EstadoSolicitud): string {
    const map: Record<string, string> = {
      REGISTRADA: 'badge--registrada', CLASIFICADA: 'badge--clasificada',
      EN_ATENCION: 'badge--en-atencion', ATENDIDA: 'badge--atendida',
      CERRADA: 'badge--cerrada', RECHAZADA: 'badge--rechazada',
      CANCELADA: 'badge--cancelada', VENCIDA: 'badge--vencida',
      ESCALADA: 'badge--escalada', IMPUGNADA: 'badge--impugnada',
    };
    return `badge ${map[estado] ?? ''}`;
  }

  getPrioridadClass(nivel?: NivelPrioridad): string {
    if (!nivel) return 'prio-badge';
    const map: Record<string, string> = {
      CRITICA: 'prio-badge--critica', ALTA: 'prio-badge--alta',
      MEDIA: 'prio-badge--media', BAJA: 'prio-badge--baja',
    };
    return `prio-badge ${map[nivel] ?? ''}`;
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('es-CO', {
      day: '2-digit', month: 'short', year: 'numeric',
    });
  }

  Math = Math;

  get canCreate(): boolean {
    return this.auth.hasAnyRole(NombreRol.ESTUDIANTE, NombreRol.DOCENTE, NombreRol.ADMINISTRATIVO);
  }
}
