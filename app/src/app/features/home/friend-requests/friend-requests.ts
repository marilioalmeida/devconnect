import { Component, inject, output, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { firstValueFrom } from 'rxjs';
import { FriendshipApi } from '../../../core/friendship/friendship-api';
import { PendingRequests } from '../../../core/friendship/pending-requests';
import { Notifier } from '../../../core/notification/notifier';
import { RelativeTimePipe } from '../../../core/time/relative-time-pipe';
import {
  ConfirmDialogData,
  ConfirmDialog,
} from '../../../shared/confirm-dialog/confirm-dialog';
import { LoadError } from '../../../shared/load-error/load-error';
import { UserRow } from '../../../shared/user-row/user-row';

@Component({
  selector: 'app-friend-requests',
  imports: [
    RelativeTimePipe,
    UserRow,
    LoadError,
    MatButtonModule,
    MatCardModule,
    MatProgressBarModule,
  ],
  templateUrl: './friend-requests.html',
  styleUrl: './friend-requests.scss',
})
export class FriendRequests {
  readonly friendshipAccepted = output<void>();

  private readonly friendshipApi = inject(FriendshipApi);
  private readonly notifier = inject(Notifier);
  private readonly dialog = inject(MatDialog);

  protected readonly requests = inject(PendingRequests).resource;

  protected readonly busy = signal(false);

  protected async accept(friendshipId: number, name: string): Promise<void> {
    this.busy.set(true);

    const accepted = await firstValueFrom(this.friendshipApi.accept(friendshipId))
      .then(() => true)
      .catch(() => false);

    this.busy.set(false);

    if (!accepted) {
      return;
    }

    this.notifier.success(`You and ${name} are now friends`);
    this.requests.reload();
    this.friendshipAccepted.emit();
  }

  protected async decline(friendshipId: number, name: string): Promise<void> {
    const data: ConfirmDialogData = {
      title: 'Decline request',
      message: `The request from ${name} will be removed. They can send a new one later.`,
      action: 'Decline',
    };

    const confirmed = await firstValueFrom(
      this.dialog.open(ConfirmDialog, { data: data }).afterClosed(),
    );

    if (!confirmed) {
      return;
    }

    this.busy.set(true);

    const declined = await firstValueFrom(this.friendshipApi.remove(friendshipId))
      .then(() => true)
      .catch(() => false);

    this.busy.set(false);

    if (declined) {
      this.notifier.success('Request declined');
      this.requests.reload();
    }
  }
}
