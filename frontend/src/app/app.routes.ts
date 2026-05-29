import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';
import { NombreRol } from './core/models';
import { MainLayoutComponent } from './shared/components/layout/main-layout/main-layout.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'solicitudes',
        loadComponent: () => import('./features/solicitudes/lista/solicitudes-lista.component').then(m => m.SolicitudesListaComponent),
      },
      {
        path: 'solicitudes/nueva',
        loadComponent: () => import('./features/solicitudes/nueva/solicitud-nueva.component').then(m => m.SolicitudNuevaComponent),
        canActivate: [roleGuard(NombreRol.ESTUDIANTE, NombreRol.DOCENTE, NombreRol.ADMINISTRATIVO)],
      },
      {
        path: 'solicitudes/:id',
        loadComponent: () => import('./features/solicitudes/detalle/solicitud-detalle.component').then(m => m.SolicitudDetalleComponent),
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./features/usuarios/usuarios.component').then(m => m.UsuariosComponent),
        canActivate: [roleGuard(NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR)],
      },
      {
        path: 'catalogo/tipos',
        loadComponent: () => import('./features/catalogo/tipos/tipos-solicitud.component').then(m => m.TiposSolicitudComponent),
        canActivate: [roleGuard(NombreRol.ADMINISTRATIVO, NombreRol.COORDINADOR, NombreRol.DIRECTOR)],
      },
      {
        path: 'catalogo/reglas',
        loadComponent: () => import('./features/catalogo/reglas/reglas-prioridad.component').then(m => m.ReglasPrioridadComponent),
        canActivate: [roleGuard(NombreRol.ADMINISTRATIVO, NombreRol.COORDINADOR, NombreRol.DIRECTOR)],
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
