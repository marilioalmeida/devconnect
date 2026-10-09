import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { firstValueFrom } from 'rxjs';
import { Auth } from '../../core/auth/auth';
import { CommentApi } from '../../core/comment/comment-api';
import { PostComment } from '../../core/models/comment';
import { Notifier } from '../../core/notification/notifier';
import { RelativeTimePipe } from '../../core/time/relative-time-pipe';
import { Avatar } from '../avatar/avatar';
import { PostContent } from '../post-content/post-content';
import {
  ConfirmDialogData,
  ConfirmDialog,
} from '../confirm-dialog/confirm-dialog';
import { LoadError } from '../load-error/load-error';

const PAGE_STEP = 5;

@Component({
  selector: 'app-post-comments',
  imports: [
    RelativeTimePipe,
    RouterLink,
    Avatar,
    PostContent,
    LoadError,
    FormField,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './post-comments.html',
  styleUrl: './post-comments.scss',
})
export class PostComments {
  readonly postId = input.required<number>();
  readonly postAuthorId = input.required<number>();
  readonly commentCreated = output<void>();
  readonly commentRemoved = output<void>();

  private readonly commentApi = inject(CommentApi);
  private readonly notifier = inject(Notifier);
  private readonly dialog = inject(MatDialog);

  protected readonly user = inject(Auth).user;

  protected readonly quantity = signal(PAGE_STEP);
  protected readonly submitting = signal(false);
  protected readonly removing = signal(false);

  protected readonly comments = this.commentApi.postComments(
    this.postId,
    this.quantity,
  );

  protected readonly hasMore = computed(() => {
    const page = this.comments.value();
    return page.content.length < page.page.totalElements;
  });

  protected readonly draft = signal({ content: '' });

  protected readonly form = form(this.draft, (path) => {
    required(path.content, { message: 'Write a comment' });
    maxLength(path.content, 500, { message: 'Comments can have at most 500 characters' });
  });

  protected canDelete(comment: PostComment): boolean {
    const myId = this.user()?.id;

    return myId !== undefined && (comment.author.id === myId || this.postAuthorId() === myId);
  }

  protected submit(): void {
    submit(this.form, async () => {
      const content = this.draft().content.trim();

      this.submitting.set(true);

      const created = await firstValueFrom(this.commentApi.create(this.postId(), { content }))
        .then(() => true)
        .catch(() => false);

      this.submitting.set(false);

      if (!created) {
        return;
      }

      this.draft.set({ content: '' });
      this.comments.reload();
      this.commentCreated.emit();
    });
  }

  protected async remove(comment: PostComment): Promise<void> {
    const data: ConfirmDialogData = {
      title: 'Delete comment',
      message: 'The comment will be deleted. This cannot be undone.',
      action: 'Delete',
    };

    const confirmed = await firstValueFrom(
      this.dialog.open(ConfirmDialog, { data: data }).afterClosed(),
    );

    if (!confirmed) {
      return;
    }

    this.removing.set(true);

    const removed = await firstValueFrom(this.commentApi.remove(this.postId(), comment.id))
      .then(() => true)
      .catch(() => false);

    this.removing.set(false);

    if (removed) {
      this.notifier.success('Comment deleted');
      this.comments.reload();
      this.commentRemoved.emit();
    }
  }

  protected loadMore(): void {
    this.quantity.update((current) => current + PAGE_STEP);
  }
}
