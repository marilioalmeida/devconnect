import { Component, inject, signal } from '@angular/core';
import {
  FormField,
  email,
  form,
  maxLength,
  minLength,
  required,
  submit,
  validate,
} from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Auth } from '../../core/auth/auth';
import { NewUser } from '../../core/models/user';
import { Notifier } from '../../core/notification/notifier';
import { UserApi } from '../../core/user/user-api';
import { Avatar } from '../../shared/avatar/avatar';

@Component({
  selector: 'app-signup',
  imports: [
    FormField,
    RouterLink,
    Avatar,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './signup.html',
  styleUrl: './signup.scss',
})
export class Signup {
  private readonly userApi = inject(UserApi);
  private readonly auth = inject(Auth);
  private readonly notifier = inject(Notifier);
  private readonly router = inject(Router);

  protected readonly submitting = signal(false);

  protected readonly yesterday = yesterday();

  protected readonly data = signal({
    fullName: '',
    email: '',
    nickname: '',
    birthDate: null as Date | null,
    password: '',
    passwordConfirmation: '',
    profileImage: '',
  });

  protected readonly form = form(this.data, (path) => {
    required(path.fullName, { message: 'Enter your full name' });
    maxLength(path.fullName, 255, { message: 'Name must be at most 255 characters' });

    required(path.email, { message: 'Enter your email' });
    email(path.email, { message: 'Enter a valid email' });
    maxLength(path.email, 255, { message: 'Email must be at most 255 characters' });

    maxLength(path.nickname, 50, { message: 'Nickname must be at most 50 characters' });

    required(path.birthDate, { message: 'Enter your birth date' });
    validate(path.birthDate, ({ value }) => {
      const date = value();

      if (date !== null && date >= today()) {
        return { kind: 'notPastDate', message: 'Date must be before today' };
      }

      return undefined;
    });

    required(path.password, { message: 'Enter a password' });
    minLength(path.password, 8, { message: 'Password must be at least 8 characters' });
    maxLength(path.password, 128, { message: 'Password must be at most 128 characters' });

    required(path.passwordConfirmation, { message: 'Repeat the password' });
    validate(path.passwordConfirmation, ({ value, valueOf }) => {
      if (value() !== valueOf(path.password)) {
        return { kind: 'passwordMismatch', message: 'Passwords do not match' };
      }

      return undefined;
    });

    maxLength(path.profileImage, 512, {
      message: 'Image URL must be at most 512 characters',
    });
  });

  protected register(): void {
    submit(this.form, async () => {
      const values = this.data();

      if (values.birthDate === null) {
        return;
      }

      const newUser: NewUser = {
        fullName: values.fullName.trim(),
        email: values.email.trim().toLowerCase(),
        nickname: textOrNull(values.nickname),
        birthDate: toIsoDate(values.birthDate),
        password: values.password,
        profileImage: textOrNull(values.profileImage),
      };

      this.submitting.set(true);

      try {
        await firstValueFrom(this.userApi.register(newUser));
      } catch {
        this.submitting.set(false);
        return;
      }

      try {
        await firstValueFrom(
          this.auth.signIn({ email: newUser.email, password: newUser.password }),
        );
        this.notifier.success('Account created. Welcome to DevConnect!');
        await this.router.navigateByUrl('/home');
      } catch {
        this.submitting.set(false);
        this.notifier.success('Account created. Log in with your email and password.');
        await this.router.navigateByUrl('/login');
      }
    });
  }
}

function today(): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return date;
}

function yesterday(): Date {
  const date = today();
  date.setDate(date.getDate() - 1);
  return date;
}

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function textOrNull(value: string): string | null {
  const trimmed = value.trim();

  return trimmed.length > 0 ? trimmed : null;
}
