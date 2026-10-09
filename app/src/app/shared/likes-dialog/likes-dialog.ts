import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { LikeApi } from '../../core/like/like-api';
import { LoadError } from '../load-error/load-error';
import { UserRow } from '../user-row/user-row';

const PAGE_STEP = 10;

export interface LikesDialogData {
  postId: number;
}

@Component({
  selector: 'app-likes-dialog',
  imports: [
    UserRow,
    LoadError,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './likes-dialog.html',
  styleUrl: './likes-dialog.scss',
})
export class LikesDialog {
  private readonly data = inject<LikesDialogData>(MAT_DIALOG_DATA);

  protected readonly postId = signal(this.data.postId);
  protected readonly quantity = signal(PAGE_STEP);

  protected readonly likes = inject(LikeApi).likers(this.postId, this.quantity);

  protected readonly hasMore = computed(() => {
    const page = this.likes.value();
    return page.content.length < page.page.totalElements;
  });

  protected loadMore(): void {
    this.quantity.update((current) => current + PAGE_STEP);
  }
}
