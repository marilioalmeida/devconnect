import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { FriendshipApi } from '../../core/friendship/friendship-api';
import { Notifier } from '../../core/notification/notifier';
import { PostApi } from '../../core/post/post-api';
import { UserApi } from '../../core/user/user-api';
import { Avatar } from '../../shared/avatar/avatar';
import { LoadError } from '../../shared/load-error/load-error';
import { hueOf } from '../../shared/avatar/visual-identity';
import { PostCard } from '../../shared/post-card/post-card';
import {
  ConfirmDialogData,
  ConfirmDialog,
} from '../../shared/confirm-dialog/confirm-dialog';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-profile',
  imports: [
    Avatar,
    LoadError,
    PostCard,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class ProfilePage {
  readonly id = input.required<string>();

  private readonly userApi = inject(UserApi);
  private readonly postApi = inject(PostApi);
  private readonly friendshipApi = inject(FriendshipApi);
  private readonly notifier = inject(Notifier);
  private readonly dialog = inject(MatDialog);

  protected readonly userId = computed(() => Number(this.id()));

  protected readonly size = signal(DEFAULT_PAGE_SIZE);
  protected readonly pageSizeOptions = [5, 10, 20];
  protected readonly busy = signal(false);

  protected readonly page = linkedSignal({
    source: this.userId,
    computation: () => 0,
  });

  protected readonly profile = this.userApi.profileById(this.userId);

  protected readonly posts = this.postApi.userPosts(this.userId, this.page, this.size);

  protected readonly status = computed(() =>
    this.profile.hasValue() ? this.profile.value().relationship.status : null,
  );

  protected readonly isOwnProfile = computed(() => this.status() === 'OWN_PROFILE');

  protected readonly hue = computed(() =>
    hueOf(this.profile.hasValue() ? this.profile.value().fullName : ''),
  );

  protected readonly postCount = computed(() =>
    this.posts.hasValue() ? this.posts.value().page.totalElements : 0,
  );

  protected readonly profileNotFound = computed(() => {
    const error = this.profile.error();

    return error instanceof HttpErrorResponse && error.status === 404;
  });

  protected onPostRemoved(): void {
    const { number, totalElements, size } = this.posts.value().page;
    const lastPage = Math.max(0, Math.ceil((totalElements - 1) / size) - 1);

    if (number > lastPage) {
      this.page.set(lastPage);
      return;
    }

    this.posts.reload();
  }

  protected async sendRequest(): Promise<void> {
    this.busy.set(true);

    const sent = await firstValueFrom(this.friendshipApi.sendRequest(this.userId()))
      .then(() => true)
      .catch(() => false);

    this.busy.set(false);

    if (sent) {
      this.notifier.success('Friend request sent');
      this.profile.reload();
    }
  }

  protected async accept(): Promise<void> {
    const friendshipId = this.profile.value()?.relationship.friendshipId;

    if (friendshipId === null || friendshipId === undefined) {
      return;
    }

    this.busy.set(true);

    const accepted = await firstValueFrom(this.friendshipApi.accept(friendshipId))
      .then(() => true)
      .catch(() => false);

    this.busy.set(false);

    if (accepted) {
      this.notifier.success('You are now friends');
      this.profile.reload();
      this.posts.reload();
    }
  }

  protected async remove(): Promise<void> {
    const profile = this.profile.value();
    const friendshipId = profile?.relationship.friendshipId;

    if (!profile || friendshipId === null || friendshipId === undefined) {
      return;
    }

    const undoing = profile.relationship.status === 'FRIENDS';

    const data: ConfirmDialogData = undoing
      ? {
          title: 'Remove friend',
          message: `You will stop seeing ${profile.fullName}'s private posts, and they will stop seeing yours.`,
          action: 'Remove',
        }
      : {
          title: 'Cancel request',
          message: `The friend request sent to ${profile.fullName} will be removed.`,
          action: 'Cancel request',
        };

    const confirmed = await firstValueFrom(
      this.dialog.open(ConfirmDialog, { data: data }).afterClosed(),
    );

    if (!confirmed) {
      return;
    }

    this.busy.set(true);

    const removed = await firstValueFrom(this.friendshipApi.remove(friendshipId))
      .then(() => true)
      .catch(() => false);

    this.busy.set(false);

    if (removed) {
      this.notifier.success(undoing ? 'Friend removed' : 'Request cancelled');
      this.profile.reload();
      this.posts.reload();
    }
  }

  protected changePage(event: PageEvent): void {
    this.size.set(event.pageSize);
    this.page.set(event.pageIndex);
  }
}
