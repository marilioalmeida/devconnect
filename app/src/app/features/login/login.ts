import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { FormField, email, form, required, submit } from '@angular/forms/signals';
import { firstValueFrom } from 'rxjs';
import { Auth } from '../../core/auth/auth';

@Component({
  selector: 'app-login',
  imports: [
    FormField,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);

  protected readonly credentials = signal({ email: '', password: '' });

  protected readonly form = form(this.credentials, (path) => {
    required(path.email, { message: 'Enter your email' });
    email(path.email, { message: 'Enter a valid email' });
    required(path.password, { message: 'Enter your password' });
  });

  protected signIn(): void {
    submit(this.form, async () => {
      this.submitting.set(true);

      const { email, password } = this.credentials();

      try {
        await firstValueFrom(this.auth.signIn({ email: email.trim().toLowerCase(), password }));
        await this.router.navigateByUrl('/home');
      } catch {
        this.submitting.set(false);
      }
    });
  }
}
