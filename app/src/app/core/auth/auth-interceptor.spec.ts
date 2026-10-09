import { HttpHandlerFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Auth } from './auth';
import { authInterceptor } from './auth-interceptor';

const API = 'http://localhost:8080';

describe('authInterceptor', () => {
  let sent: HttpRequest<unknown>;

  const next: HttpHandlerFn = (req) => {
    sent = req;
    return of(new HttpResponse());
  };

  function intercept(req: HttpRequest<unknown>, token: string | null = 'valid-jwt') {
    TestBed.configureTestingModule({
      providers: [{ provide: Auth, useValue: { token: () => token } }],
    });

    TestBed.runInInjectionContext(() => authInterceptor(req, next).subscribe());
  }

  afterEach(() => TestBed.resetTestingModule());

  it('sends the Bearer token on protected routes', () => {
    intercept(new HttpRequest('GET', `${API}/users/me`));

    expect(sent.headers.get('Authorization')).toBe('Bearer valid-jwt');
  });

  it('does not send the Bearer token on login, otherwise an expired token would lock the user out', () => {
    intercept(new HttpRequest('POST', `${API}/login`, {}));

    expect(sent.headers.has('Authorization')).toBe(false);
  });

  it('does not send the Bearer token on user signup', () => {
    intercept(new HttpRequest('POST', `${API}/users`, {}));

    expect(sent.headers.has('Authorization')).toBe(false);
  });

  it('sends the Bearer token on user search, which shares the signup path with another verb', () => {
    intercept(new HttpRequest('GET', `${API}/users`));

    expect(sent.headers.get('Authorization')).toBe('Bearer valid-jwt');
  });

  it('does not send the Bearer token when there is no token', () => {
    intercept(new HttpRequest('GET', `${API}/users/me`), null);

    expect(sent.headers.has('Authorization')).toBe(false);
  });
});
