import { DatePipe } from '@angular/common';
import { Component, computed, inject, input, linkedSignal, output, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Auth } from '../../core/auth/auth';
import { LikeApi } from '../../core/like/like-api';
import { Post, Visibility } from '../../core/models/post';
import { Notifier } from '../../core/notification/notifier';
import { PostApi } from '../../core/post/post-api';
import { RelativeTimePipe } from '../../core/time/relative-time-pipe';
import { Avatar } from '../avatar/avatar';
import { PostContent } from '../post-content/post-content';
import { LikesDialog, LikesDialogData } from '../likes-dialog/likes-dialog';
import { ConfirmDialogData, ConfirmDialog } from '../confirm-dialog/confirm-dialog';
import { PostComments } from '../post-comments/post-comments';

@Component({
  selector: 'app-post-card',
  imports: [
    DatePipe,
    RelativeTimePipe,
    RouterLink,
    Avatar,
    PostContent,
    PostComments,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
  ],
  templateUrl: './post-card.html',
  styleUrl: './post-card.scss',
})
export class PostCard {
  readonly post = input.required<Post>();
  readonly removed = output<number>();

  private readonly postApi = inject(PostApi);
  private readonly likeApi = inject(LikeApi);
  private readonly auth = inject(Auth);
  private readonly notifier = inject(Notifier);
  private readonly dialog = inject(MatDialog);

  protected readonly current = linkedSignal(() => this.post());

  protected readonly busy = signal(false);
  protected readonly liking = signal(false);
  protected readonly commentsOpen = signal(false);
  protected readonly pulse = signal(false);
  protected readonly editing = signal(false);
  protected readonly draft = signal('');

  protected readonly isOwnPost = computed(
    () => this.current().author.id === this.auth.user()?.id,
  );

  protected readonly isPublic = computed(() => this.current().visibility === 'PUBLIC');

  protected async toggleLike(): Promise<void> {
    const post = this.current();

    this.liking.set(true);

    const summary = await firstValueFrom(
      post.likedByCurrentUser
        ? this.likeApi.unlike(post.id)
        : this.likeApi.like(post.id),
    ).catch(() => null);

    this.liking.set(false);

    if (summary === null) {
      return;
    }

    if (summary.likedByCurrentUser) {
      this.pulse.set(true);
      setTimeout(() => this.pulse.set(false), 350);
    }

    this.current.update((current) => ({
      ...current,
      likeCount: summary.likeCount,
      likedByCurrentUser: summary.likedByCurrentUser,
    }));
  }

  protected toggleComments(): void {
    this.commentsOpen.update((opened) => !opened);
  }

  protected onCommentCreated(): void {
    this.current.update((current) => ({
      ...current,
      commentCount: current.commentCount + 1,
    }));
  }

  protected onCommentRemoved(): void {
    this.current.update((current) => ({
      ...current,
      commentCount: Math.max(0, current.commentCount - 1),
    }));
  }

  protected openLikes(): void {
    const data: LikesDialogData = { postId: this.current().id };

    this.dialog.open(LikesDialog, { data: data });
  }

  protected startEdit(): void {
    this.draft.set(this.current().content);
    this.editing.set(true);
  }

  protected cancelEdit(): void {
    this.editing.set(false);
  }

  protected async saveEdit(): Promise<void> {
    const content = this.draft().trim();

    if (!content) {
      return;
    }

    this.busy.set(true);

    const updated = await firstValueFrom(
      this.postApi.updateContent(this.current().id, content),
    ).catch(() => null);

    this.busy.set(false);

    if (updated === null) {
      return;
    }

    this.current.set(updated);
    this.editing.set(false);
    this.notifier.success('Post updated');
  }

  protected async toggleVisibility(): Promise<void> {
    const destination: Visibility = this.isPublic() ? 'PRIVATE' : 'PUBLIC';

    this.busy.set(true);

    const updated = await firstValueFrom(
      this.postApi.updateVisibility(this.current().id, destination),
    ).catch(() => null);

    this.busy.set(false);

    if (updated === null) {
      return;
    }

    this.current.set(updated);
    this.notifier.success(destination === 'PUBLIC' ? 'Post is now public' : 'Post is now private');
  }

  protected async remove(): Promise<void> {
    const data: ConfirmDialogData = {
      title: 'Delete post',
      message:
        'The post will be deleted along with its likes and comments. This cannot be undone.',
      action: 'Delete',
    };

    const confirmed = await firstValueFrom(
      this.dialog.open(ConfirmDialog, { data: data }).afterClosed(),
    );

    if (!confirmed) {
      return;
    }

    const id = this.current().id;
    this.busy.set(true);

    const removed = await firstValueFrom(this.postApi.remove(id))
      .then(() => true)
      .catch(() => false);

    this.busy.set(false);

    if (removed) {
      this.notifier.success('Post deleted');
      this.removed.emit(id);
    }
  }
}
