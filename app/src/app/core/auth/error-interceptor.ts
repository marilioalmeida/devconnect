import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { Notifier } from '../notification/notifier';
import { Auth } from './auth';
import { SKIP_GLOBAL_ERROR_HANDLER } from './http-context';

const MESSAGE_BY_STATUS: Record<number, string> = {
  401: 'Your session has expired. Please log in again.',
  403: 'You do not have permission to do this',
  404: 'Resource not found',
  500: 'Unexpected server error',
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const notifier = inject(Notifier);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (req.context.get(SKIP_GLOBAL_ERROR_HANDLER)) {
        return throwError(() => error);
      }

      if (error.status === 401 && !req.url.endsWith('/login')) {
        auth.endSession();
        router.navigateByUrl('/login', { replaceUrl: true });
      }

      notifier.error(messageOf(error));

      return throwError(() => error);
    }),
  );
};

function messageOf(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return 'Could not connect to the server. Check that the API is running.';
  }

  return error.error?.message ?? MESSAGE_BY_STATUS[error.status] ?? 'An unexpected error occurred';
}
