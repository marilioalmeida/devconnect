import { DatePipe } from '@angular/common';
import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { FormField, debounce, form } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { FriendshipApi } from '../../core/friendship/friendship-api';
import { Friend } from '../../core/models/friendship';
import { Notifier } from '../../core/notification/notifier';
import {
  ConfirmDialogData,
  ConfirmDialog,
} from '../../shared/confirm-dialog/confirm-dialog';
import { UserRow } from '../../shared/user-row/user-row';
import { LoadError } from '../../shared/load-error/load-error';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-friends',
  imports: [
    DatePipe,
    FormField,
    RouterLink,
    UserRow,
    LoadError,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
  ],
  templateUrl: './friends.html',
  styleUrl: './friends.scss',
})
export class Friends {
  private readonly friendshipApi = inject(FriendshipApi);
  private readonly notifier = inject(Notifier);
  private readonly dialog = inject(MatDialog);

  protected readonly size = signal(DEFAULT_PAGE_SIZE);
  protected readonly pageSizeOptions = [5, 10, 20];
  protected readonly undoing = signal<number | null>(null);

  protected readonly searchModel = signal({ search: '' });

  protected readonly form = form(this.searchModel, (path) => {
    debounce(path.search, 400);
  });

  private readonly search = computed(() => this.searchModel().search.trim());

  protected readonly page = linkedSignal({
    source: this.search,
    computation: () => 0,
  });

  protected readonly friends = this.friendshipApi.pagedFriends(
    this.search,
    this.page,
    this.size,
  );

  protected async undo(relationship: Friend): Promise<void> {
    const data: ConfirmDialogData = {
      title: 'Remove friend',
      message: `You will stop seeing ${relationship.friend.fullName}'s private posts, and they will stop seeing yours.`,
      action: 'Remove',
    };

    const confirmed = await firstValueFrom(
      this.dialog.open(ConfirmDialog, { data: data }).afterClosed(),
    );

    if (!confirmed) {
      return;
    }

    this.undoing.set(relationship.id);

    const removed = await firstValueFrom(this.friendshipApi.remove(relationship.id))
      .then(() => true)
      .catch(() => false);

    this.undoing.set(null);

    if (removed) {
      this.notifier.success(`You are no longer friends with ${relationship.friend.fullName}`);
      this.reload();
    }
  }

  protected changePage(event: PageEvent): void {
    this.size.set(event.pageSize);
    this.page.set(event.pageIndex);
  }

  private reload(): void {
    if (this.friends.value().content.length === 1 && this.page() > 0) {
      this.page.update((current) => current - 1);
      return;
    }

    this.friends.reload();
  }
}
