import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from './auth';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(Auth).token();

  if (token === null || isPublicRoute(req.url, req.method)) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};

function isPublicRoute(url: string, method: string): boolean {
  if (method !== 'POST') {
    return false;
  }

  return url.endsWith('/login') || url.endsWith('/users');
}
