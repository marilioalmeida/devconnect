import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { Auth } from './core/auth/auth';
import { Theme } from './core/theme/theme';
import { Avatar } from './shared/avatar/avatar';

interface Destination {
  route: string;
  label: string;
  icon: string;
}

const DESTINATIONS: Destination[] = [
  { route: '/home', label: 'Feed', icon: 'dynamic_feed' },
];

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    Avatar,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatMenuModule,
    MatSidenavModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  private readonly breakpoints = inject(BreakpointObserver);

  protected readonly theme = inject(Theme);

  protected readonly destinations = DESTINATIONS;

  protected readonly user = this.auth.user;

  private readonly narrowScreen = toSignal(
    this.breakpoints.observe(Breakpoints.Handset).pipe(map((state) => state.matches)),
    { initialValue: false },
  );

  protected readonly mode = computed(() => (this.narrowScreen() ? 'over' : 'side'));

  protected readonly opened = computed(() => this.user() !== null && !this.narrowScreen());

  protected onNavigate(drawer: { close: () => void }): void {
    if (this.narrowScreen()) {
      drawer.close();
    }
  }

  protected async logout(): Promise<void> {
    this.auth.endSession();
    await this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
