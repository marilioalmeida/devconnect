import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from './auth';

export const authenticatedGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.hasValidSession()) {
    return router.parseUrl('/login');
  }

  if (auth.user() === null) {
    void auth.restoreSession();
  }

  return true;
};
