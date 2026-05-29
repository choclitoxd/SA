import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SolicitudesService } from '../../../core/services/solicitudes.service';
import { TiposSolicitudService } from '../../../core/services/tipos-solicitud.service';
import { UsuariosService } from '../../../core/services/usuarios.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  SolicitudDetalleResponse,
  EstadoSolicitud,
  NivelPrioridad,
  MotivoRechazo,
  TipoSolicitudResponse,
  UsuarioResponse,
  ESTADO_LABELS,
  PRIORIDAD_LABELS,
  CANAL_LABELS,
  MOTIVO_LABELS,
  NombreRol,
} from '../../../core/models';

type ModalType = 'clasificar' | 'asignar' | 'atender' | 'cerrar' | 'rechazar' | null;

@Component({
  selector: 'app-solicitud-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './solicitud-detalle.component.html',
  styleUrl: './solicitud-detalle.component.scss',
})
export class SolicitudDetalleComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(SolicitudesService);
  private tiposService = inject(TiposSolicitudService);
  private usuariosService = inject(UsuariosService);
  auth = inject(AuthService);

  loading = signal(true);
  submitting = signal(false);
  solicitud = signal<SolicitudDetalleResponse | null>(null);
  error = signal('');
  activeModal = signal<ModalType>(null);

  tipos = signal<TipoSolicitudResponse[]>([]);
  usuarios = signal<UsuarioResponse[]>([]);

  estadoLabels = ESTADO_LABELS;
  prioridadLabels = PRIORIDAD_LABELS;
  canalLabels = CANAL_LABELS;
  motivoLabels = MOTIVO_LABELS;
  prioridades = Object.values(NivelPrioridad);
  motivos = Object.values(MotivoRechazo);
  NombreRol = NombreRol;
  EstadoSolicitud = EstadoSolicitud;

  // Form models
  form = {
    tipoSolicitudId: 0,
    nivelPrioridad: '' as NivelPrioridad | '',
    justificacionPrioridad: '',
    sugerenciaIaId: undefined as number | undefined,
    responsableId: 0,
    notas: '',
    observacionResolucion: '',
    observacionCierre: '',
    motivo: '' as MotivoRechazo | '',
    justificacionRechazo: '',
  };

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  load(id: number) {
    this.loading.set(true);
    this.service.obtener(id).subscribe({
      next: (s) => {
        this.solicitud.set(s);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la solicitud.');
        this.loading.set(false);
      },
    });
  }

  openModal(type: ModalType) {
    this.error.set('');
    this.activeModal.set(type);
    if (type === 'clasificar') this.loadTipos();
    if (type === 'asignar') this.loadUsuarios();
  }

  closeModal() {
    this.activeModal.set(null);
    this.form.justificacionPrioridad = '';
    this.form.observacionResolucion = '';
    this.form.observacionCierre = '';
    this.form.justificacionRechazo = '';
    this.form.notas = '';
    this.form.nivelPrioridad = '';
    this.form.motivo = '';
  }

  private loadTipos() {
    if (this.tipos().length > 0) return;
    this.tiposService.listar(true).subscribe({ next: (t) => this.tipos.set(t) });
  }

  private loadUsuarios() {
    if (this.usuarios().length > 0) return;
    this.usuariosService.listar(0, 100).subscribe({ next: (p) => this.usuarios.set(p.content) });
  }

  submitClasificar() {
    const s = this.solicitud()!;
    this.submitting.set(true);
    this.service.clasificar(s.id, {
      tipoSolicitudId: this.form.tipoSolicitudId,
      nivelPrioridad: this.form.nivelPrioridad || undefined,
      justificacionPrioridad: this.form.justificacionPrioridad || undefined,
      sugerenciaIaId: s.sugerenciaIA?.id,
      version: s.version,
    }).subscribe({ next: (r) => this.onSuccess(r), error: (e) => this.onError(e) });
  }

  submitAsignar() {
    const s = this.solicitud()!;
    this.submitting.set(true);
    this.service.asignar(s.id, {
      responsableId: this.form.responsableId,
      notas: this.form.notas || undefined,
      version: s.version,
    }).subscribe({ next: (r) => this.onSuccess(r), error: (e) => this.onError(e) });
  }

  submitAtender() {
    const s = this.solicitud()!;
    this.submitting.set(true);
    this.service.marcarAtendida(s.id, {
      observacionResolucion: this.form.observacionResolucion,
      version: s.version,
    }).subscribe({ next: (r) => this.onSuccess(r), error: (e) => this.onError(e) });
  }

  submitCerrar() {
    const s = this.solicitud()!;
    this.submitting.set(true);
    this.service.cerrar(s.id, {
      observacionCierre: this.form.observacionCierre,
      version: s.version,
    }).subscribe({ next: (r) => this.onSuccess(r), error: (e) => this.onError(e) });
  }

  submitRechazar() {
    const s = this.solicitud()!;
    this.submitting.set(true);
    this.service.rechazar(s.id, {
      motivo: this.form.motivo as MotivoRechazo,
      justificacion: this.form.justificacionRechazo,
      version: s.version,
    }).subscribe({ next: (r) => this.onSuccess(r), error: (e) => this.onError(e) });
  }

  private onSuccess(_r: SolicitudDetalleResponse) {
    this.submitting.set(false);
    this.closeModal();
    // Re-fetch para obtener la versión exacta que tiene la DB tras todas las operaciones internas
    this.load(this.solicitud()!.id);
  }

  private onError(e: any) {
    this.submitting.set(false);
    this.error.set(e?.error?.message ?? e?.error?.error ?? 'Error al procesar la acción.');
  }

  getEstadoClass(estado: EstadoSolicitud): string {
    const map: Record<string, string> = {
      REGISTRADA: 'badge--registrada', CLASIFICADA: 'badge--clasificada',
      EN_ATENCION: 'badge--en-atencion', ATENDIDA: 'badge--atendida',
      CERRADA: 'badge--cerrada', RECHAZADA: 'badge--rechazada',
      CANCELADA: 'badge--cancelada', VENCIDA: 'badge--vencida',
      ESCALADA: 'badge--escalada', IMPUGNADA: 'badge--impugnada',
    };
    return `badge badge--lg ${map[estado] ?? ''}`;
  }

  getPrioridadClass(nivel?: NivelPrioridad | null): string {
    if (!nivel) return 'prio-badge';
    const map: Record<string, string> = {
      CRITICA: 'prio-badge--critica', ALTA: 'prio-badge--alta',
      MEDIA: 'prio-badge--media', BAJA: 'prio-badge--baja',
    };
    return `prio-badge prio-badge--lg ${map[nivel] ?? ''}`;
  }

  formatDateTime(dateStr: string): string {
    return new Date(dateStr).toLocaleString('es-CO', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('es-CO', {
      day: '2-digit', month: 'short', year: 'numeric',
    });
  }

  get canClasificar(): boolean {
    const s = this.solicitud();
    return !!s &&
      s.estado === EstadoSolicitud.REGISTRADA &&
      this.auth.hasAnyRole(NombreRol.COORDINADOR, NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR);
  }

  get canAsignar(): boolean {
    const s = this.solicitud();
    return !!s &&
      s.estado === EstadoSolicitud.CLASIFICADA &&
      this.auth.hasAnyRole(NombreRol.COORDINADOR, NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR);
  }

  get canAtender(): boolean {
    const s = this.solicitud();
    return !!s &&
      s.estado === EstadoSolicitud.EN_ATENCION &&
      this.auth.hasAnyRole(NombreRol.DOCENTE, NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR, NombreRol.COORDINADOR);
  }

  get canCerrar(): boolean {
    const s = this.solicitud();
    return !!s &&
      s.estado === EstadoSolicitud.ATENDIDA &&
      this.auth.hasAnyRole(NombreRol.ESTUDIANTE, NombreRol.DOCENTE, NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR, NombreRol.COORDINADOR);
  }

  get canRechazar(): boolean {
    const s = this.solicitud();
    return !!s &&
      [EstadoSolicitud.REGISTRADA, EstadoSolicitud.CLASIFICADA].includes(s.estado) &&
      this.auth.hasAnyRole(NombreRol.COORDINADOR, NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR);
  }

  confidencePercent(c: number): string {
    return `${Math.round(c * 100)}%`;
  }

  goBack() {
    this.router.navigate(['/solicitudes']);
  }
}
