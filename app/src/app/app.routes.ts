import { Routes } from '@angular/router';
import { authenticatedGuard } from './core/auth/authenticated-guard';
import { guestGuard } from './core/auth/guest-guard';

export const routes: Routes = [
  {
    path: 'login',
    title: 'Log in | DevConnect',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: 'signup',
    title: 'Sign up | DevConnect',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/signup/signup').then((m) => m.Signup),
  },
  {
    path: 'home',
    title: 'DevConnect',
    canActivate: [authenticatedGuard],
    loadComponent: () => import('./features/home/home').then((m) => m.Home),
  },
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: '**', redirectTo: 'home' },
];
