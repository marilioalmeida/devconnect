import { Service, computed, effect, signal } from '@angular/core';

const THEME_KEY = 'devconnect.theme';

type ThemeScheme = 'light' | 'dark';

@Service()
export class Theme {
  private readonly _scheme = signal<ThemeScheme>(initialScheme());

  readonly dark = computed(() => this._scheme() === 'dark');

  constructor() {
    effect(() => {
      document.documentElement.style.colorScheme = this._scheme();
    });
  }

  toggle(): void {
    const next: ThemeScheme = this.dark() ? 'light' : 'dark';

    localStorage.setItem(THEME_KEY, next);
    this._scheme.set(next);
  }
}

function initialScheme(): ThemeScheme {
  const saved = localStorage.getItem(THEME_KEY);

  if (saved === 'light' || saved === 'dark') {
    return saved;
  }

  if (typeof window.matchMedia === 'function') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  return 'light';
}
