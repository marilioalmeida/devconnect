import { Service, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Service()
export class Notifier {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.open(message, 'notification-success', 4000);
  }

  error(message: string): void {
    this.open(message, 'notification-error', 6000);
  }

  private open(message: string, cssClass: string, duration: number): void {
    this.snackBar.open(message, 'Close', {
      duration: duration,
      panelClass: cssClass,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
    });
  }
}
