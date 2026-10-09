import { Component, inject, linkedSignal, signal } from '@angular/core';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { firstValueFrom } from 'rxjs';
import { Auth } from '../../core/auth/auth';
import { Notifier } from '../../core/notification/notifier';
import { UserApi } from '../../core/user/user-api';
import { Avatar } from '../../shared/avatar/avatar';

@Component({
  selector: 'app-edit-profile',
  imports: [
    FormField,
    Avatar,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './edit-profile.html',
  styleUrl: './edit-profile.scss',
})
export class EditProfile {
  private readonly userApi = inject(UserApi);
  private readonly auth = inject(Auth);
  private readonly notifier = inject(Notifier);

  protected readonly user = this.auth.user;
  protected readonly saving = signal(false);

  protected readonly data = linkedSignal(() => {
    const current = this.user();

    return {
      fullName: current?.fullName ?? '',
      nickname: current?.nickname ?? '',
      profileImage: current?.profileImage ?? '',
    };
  });

  protected readonly form = form(this.data, (path) => {
    required(path.fullName, { message: 'Enter your full name' });
    maxLength(path.fullName, 255, { message: 'Name must be at most 255 characters' });
    maxLength(path.nickname, 50, { message: 'Nickname must be at most 50 characters' });
    maxLength(path.profileImage, 512, {
      message: 'Image URL must be at most 512 characters',
    });
  });

  protected save(): void {
    submit(this.form, async () => {
      const values = this.data();

      this.saving.set(true);

      const updated = await firstValueFrom(
        this.userApi.updateProfile({
          fullName: values.fullName,
          nickname: textOrNull(values.nickname),
          profileImage: textOrNull(values.profileImage),
        }),
      ).catch(() => null);

      this.saving.set(false);

      if (updated === null) {
        return;
      }

      this.auth.updateUser(updated);
      this.notifier.success('Profile updated');
    });
  }
}

function textOrNull(value: string): string | null {
  const trimmed = value.trim();

  return trimmed.length > 0 ? trimmed : null;
}
