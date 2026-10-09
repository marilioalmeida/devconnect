import { HttpClient, HttpContext, HttpErrorResponse } from '@angular/common/http';
import { Service, inject, signal } from '@angular/core';
import { Observable, firstValueFrom, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Credentials, AccessToken } from '../models/credentials';
import { User } from '../models/user';
import { Notifier } from '../notification/notifier';
import { SKIP_GLOBAL_ERROR_HANDLER } from './http-context';

const TOKEN_KEY = 'devconnect.token';

@Service()
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly notifier = inject(Notifier);

  private readonly _token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly _user = signal<User | null>(null);

  readonly token = this._token.asReadonly();
  readonly user = this._user.asReadonly();

  hasValidSession(): boolean {
    const token = this._token();

    if (token === null) {
      return false;
    }

    if (expired(token)) {
      this.endSession();
      return false;
    }

    return true;
  }

  signIn(credentials: Credentials): Observable<User> {
    return this.http.post<AccessToken>(`${environment.apiUrl}/login`, credentials).pipe(
      tap((token) => this.storeToken(token.accessToken)),
      switchMap(() => this.loadUser()),
    );
  }

  async restoreSession(): Promise<void> {
    if (!this.hasValidSession()) {
      this.endSession();
      return;
    }

    try {
      await firstValueFrom(this.loadUser(true));
    } catch (error) {
      if (error instanceof HttpErrorResponse && (error.status === 401 || error.status === 403)) {
        this.endSession();
        return;
      }

      this.notifier.error('Could not reach the server. Please try again in a moment.');
    }
  }

  updateUser(user: User): void {
    this._user.set(user);
  }

  endSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    this._token.set(null);
    this._user.set(null);
  }

  private loadUser(silent = false): Observable<User> {
    const context = new HttpContext().set(SKIP_GLOBAL_ERROR_HANDLER, silent);

    return this.http
      .get<User>(`${environment.apiUrl}/users/me`, { context })
      .pipe(tap((user) => this._user.set(user)));
  }

  private storeToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this._token.set(token);
  }
}

function expired(token: string): boolean {
  const expiration = expirationOf(token);

  return expiration === null || expiration <= Date.now();
}

function expirationOf(token: string): number | null {
  const parts = token.split('.');

  if (parts.length !== 3) {
    return null;
  }

  try {
    const payload: unknown = JSON.parse(decode(parts[1]));

    if (typeof payload !== 'object' || payload === null) {
      return null;
    }

    const expiration = (payload as { exp?: unknown }).exp;

    return typeof expiration === 'number' ? expiration * 1000 : null;
  } catch {
    return null;
  }
}

function decode(base64Url: string): string {
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const remainder = base64.length % 4;

  return atob(remainder === 0 ? base64 : base64.padEnd(base64.length + (4 - remainder), '='));
}
