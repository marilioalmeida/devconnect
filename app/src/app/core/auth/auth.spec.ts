import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Auth } from './auth';

const TOKEN_KEY = 'devconnect.token';

function base64Url(payload: unknown): string {
  const bytes = new TextEncoder().encode(JSON.stringify(payload));
  const binary = Array.from(bytes, (byte) => String.fromCharCode(byte)).join('');

  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

function tokenWith(payload: Record<string, unknown>): string {
  return `${base64Url({ alg: 'RS256' })}.${base64Url(payload)}.signature`;
}

function secondsFromNow(seconds: number): number {
  return Math.floor(Date.now() / 1000) + seconds;
}

function createService(token: string | null): Auth {
  if (token === null) {
    localStorage.removeItem(TOKEN_KEY);
  } else {
    localStorage.setItem(TOKEN_KEY, token);
  }

  TestBed.configureTestingModule({
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });

  return TestBed.inject(Auth);
}

describe('Auth.hasValidSession', () => {
  afterEach(() => {
    localStorage.removeItem(TOKEN_KEY);
    TestBed.resetTestingModule();
  });

  it('does not consider a session without a token valid', () => {
    const auth = createService(null);

    expect(auth.hasValidSession()).toBe(false);
  });

  it('considers a session with an unexpired token valid', () => {
    const auth = createService(tokenWith({ exp: secondsFromNow(3600) }));

    expect(auth.hasValidSession()).toBe(true);
    expect(localStorage.getItem(TOKEN_KEY)).not.toBeNull();
  });

  it('rejects an expired token and clears what is left of it', () => {
    const auth = createService(tokenWith({ exp: secondsFromNow(-60) }));

    expect(auth.hasValidSession()).toBe(false);
    expect(auth.token()).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('rejects a token that does not have the three parts of a JWT', () => {
    const auth = createService('this-is-not-a-jwt');

    expect(auth.hasValidSession()).toBe(false);
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });

  it('rejects a token without the exp claim instead of treating it as eternal', () => {
    const auth = createService(tokenWith({ email: 'ana@devconnect.com' }));

    expect(auth.hasValidSession()).toBe(false);
  });

  it('reads exp even when the payload has accented characters', () => {
    const auth = createService(
      tokenWith({ sub: 'João Pedro Rocha', exp: secondsFromNow(3600) }),
    );

    expect(auth.hasValidSession()).toBe(true);
  });

  it('reads exp when the payload base64 needs padding', () => {
    const payloads = Array.from({ length: 4 }, (_, index) => ({
      exp: secondsFromNow(3600),
      padding: 'a'.repeat(index),
    }));

    for (const payload of payloads) {
      const auth = createService(tokenWith(payload));

      expect(auth.hasValidSession()).toBe(true);

      TestBed.resetTestingModule();
    }
  });
});

describe('Auth.restoreSession', () => {
  afterEach(() => {
    localStorage.removeItem(TOKEN_KEY);
    TestBed.resetTestingModule();
  });

  it('keeps the token when restoring fails because of the network', async () => {
    const auth = createService(tokenWith({ exp: secondsFromNow(3600) }));
    const http = TestBed.inject(HttpTestingController);

    const restoring = auth.restoreSession();

    http.expectOne((req) => req.url.endsWith('/users/me')).error(new ProgressEvent('error'), {
      status: 0,
    });

    await restoring;

    expect(auth.token()).not.toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).not.toBeNull();
  });

  it('ends the session when the server rejects the token', async () => {
    const auth = createService(tokenWith({ exp: secondsFromNow(3600) }));
    const http = TestBed.inject(HttpTestingController);

    const restoring = auth.restoreSession();

    http
      .expectOne((req) => req.url.endsWith('/users/me'))
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    await restoring;

    expect(auth.token()).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
  });
});
