import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { NombreRol } from '../../../../core/models';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles?: NombreRol[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  auth = inject(AuthService);

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'grid', route: '/dashboard' },
    { label: 'Solicitudes', icon: 'inbox', route: '/solicitudes' },
    {
      label: 'Nueva Solicitud',
      icon: 'plus-circle',
      route: '/solicitudes/nueva',
      roles: [NombreRol.ESTUDIANTE, NombreRol.DOCENTE, NombreRol.ADMINISTRATIVO],
    },
    {
      label: 'Usuarios',
      icon: 'users',
      route: '/usuarios',
      roles: [NombreRol.ADMINISTRATIVO, NombreRol.DIRECTOR],
    },
    {
      label: 'Tipos de Solicitud',
      icon: 'tag',
      route: '/catalogo/tipos',
      roles: [NombreRol.ADMINISTRATIVO, NombreRol.COORDINADOR, NombreRol.DIRECTOR],
    },
    {
      label: 'Reglas de Prioridad',
      icon: 'sliders',
      route: '/catalogo/reglas',
      roles: [NombreRol.ADMINISTRATIVO, NombreRol.COORDINADOR, NombreRol.DIRECTOR],
    },
  ];

  get visibleItems(): NavItem[] {
    return this.navItems.filter(item => {
      if (!item.roles) return true;
      return this.auth.hasAnyRole(...item.roles);
    });
  }

  get userInitials(): string {
    const user = this.auth.currentUser();
    if (!user) return '?';
    return `${user.nombre[0]}${user.apellido[0]}`.toUpperCase();
  }

  get userName(): string {
    const user = this.auth.currentUser();
    return user ? `${user.nombre} ${user.apellido}` : '';
  }

  get userRoleLabel(): string {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles[0] ?? '';
  }

  logout() {
    this.auth.logout();
  }

  getIcon(name: string): string {
    const icons: Record<string, string> = {
      grid: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>',
      inbox: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 002 2h16a2 2 0 002-2v-6l-3.45-6.89A2 2 0 0016.76 4H7.24a2 2 0 00-1.79 1.11z"/></svg>',
      'plus-circle': '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>',
      users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>',
      tag: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.59 13.41l-7.17 7.17a2 2 0 01-2.83 0L2 12V2h10l8.59 8.59a2 2 0 010 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>',
      sliders: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg>',
    };
    return icons[name] ?? '';
  }
}
