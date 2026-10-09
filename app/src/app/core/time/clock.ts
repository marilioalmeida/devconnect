import { DestroyRef, Service, inject, signal } from '@angular/core';

const INTERVAL_MS = 30_000;

@Service()
export class Clock {
  private readonly _now = signal(Date.now());

  readonly now = this._now.asReadonly();

  constructor() {
    const interval = setInterval(() => this._now.set(Date.now()), INTERVAL_MS);

    inject(DestroyRef).onDestroy(() => clearInterval(interval));
  }
}
