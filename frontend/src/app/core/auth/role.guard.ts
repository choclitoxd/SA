import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { NombreRol } from '../models';

export const roleGuard = (...roles: NombreRol[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isAuthenticated()) {
      router.navigate(['/login']);
      return false;
    }

    if (auth.hasAnyRole(...roles)) return true;

    router.navigate(['/']);
    return false;
  };
};
