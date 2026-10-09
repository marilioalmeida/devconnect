import { HttpContext, HttpErrorResponse, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { Notifier } from '../notification/notifier';
import { Auth } from './auth';
import { SKIP_GLOBAL_ERROR_HANDLER } from './http-context';
import { errorInterceptor } from './error-interceptor';

const API = 'http://localhost:8080';

describe('errorInterceptor', () => {
  let messages: string[];
  let destinations: string[];
  let endedSessions: number;

  function intercept(req: HttpRequest<unknown>, error: HttpErrorResponse) {
    messages = [];
    destinations = [];
    endedSessions = 0;

    const next: HttpHandlerFn = () => throwError(() => error);

    TestBed.configureTestingModule({
      providers: [
        {
          provide: Auth,
          useValue: { endSession: () => (endedSessions += 1) },
        },
        { provide: Notifier, useValue: { error: (m: string) => messages.push(m) } },
        { provide: Router, useValue: { navigateByUrl: (url: string) => destinations.push(url) } },
      ],
    });

    TestBed.runInInjectionContext(() =>
      errorInterceptor(req, next).subscribe({ error: () => undefined }),
    );
  }

  afterEach(() => TestBed.resetTestingModule());

  it('uses the API message when the error body has one', () => {
    intercept(
      new HttpRequest('POST', `${API}/login`, {}),
      new HttpErrorResponse({ status: 401, error: { message: 'Invalid email or password' } }),
    );

    expect(messages).toEqual(['Invalid email or password']);
  });

  it('falls back to the status message when a 401 has an empty body', () => {
    intercept(
      new HttpRequest('GET', `${API}/users/me`),
      new HttpErrorResponse({ status: 401, error: null }),
    );

    expect(messages).toEqual(['Your session has expired. Please log in again.']);
  });

  it('warns that the API is down when the status is zero', () => {
    intercept(
      new HttpRequest('GET', `${API}/users/me`),
      new HttpErrorResponse({ status: 0, error: null }),
    );

    expect(messages[0]).toContain('Could not connect to the server');
  });

  it('ends the session and redirects to login when the token is rejected', () => {
    intercept(
      new HttpRequest('GET', `${API}/users/me`),
      new HttpErrorResponse({ status: 401, error: null }),
    );

    expect(endedSessions).toBe(1);
    expect(destinations).toEqual(['/login']);
  });

  it('does not end the session on a 401 from login itself, so the credentials message is kept', () => {
    intercept(
      new HttpRequest('POST', `${API}/login`, {}),
      new HttpErrorResponse({ status: 401, error: { message: 'Invalid email or password' } }),
    );

    expect(endedSessions).toBe(0);
    expect(destinations).toEqual([]);
  });

  it('stays silent while restoring the session on startup', () => {
    const context = new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, true);

    intercept(
      new HttpRequest('GET', `${API}/users/me`, { context }),
      new HttpErrorResponse({ status: 401, error: null }),
    );

    expect(messages).toEqual([]);
    expect(destinations).toEqual([]);
  });
});
