import { Component, inject, signal } from '@angular/core';
import { FormField, form, maxLength, required, submit } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { firstValueFrom } from 'rxjs';
import { Auth } from '../../core/auth/auth';
import { Visibility } from '../../core/models/post';
import { Notifier } from '../../core/notification/notifier';
import { PostApi } from '../../core/post/post-api';
import { Avatar } from '../../shared/avatar/avatar';
import { PostContent } from '../../shared/post-content/post-content';
import { LoadError } from '../../shared/load-error/load-error';
import { PostCard } from '../../shared/post-card/post-card';
import { FriendRequests } from './friend-requests/friend-requests';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-home',
  imports: [
    FormField,
    RouterLink,
    Avatar,
    PostContent,
    LoadError,
    PostCard,
    FriendRequests,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly postApi = inject(PostApi);
  private readonly notifier = inject(Notifier);

  protected readonly user = inject(Auth).user;

  protected readonly page = signal(0);
  protected readonly size = signal(DEFAULT_PAGE_SIZE);
  protected readonly pageSizeOptions = [5, 10, 20];

  protected readonly feed = this.postApi.pagedFeed(this.page, this.size);

  protected readonly publishing = signal(false);
  protected readonly previewing = signal(false);

  protected readonly newPost = signal({
    content: '',
    visibility: 'PUBLIC' as Visibility,
  });

  protected readonly form = form(this.newPost, (path) => {
    required(path.content, { message: 'Write something to publish' });
    maxLength(path.content, 5000, { message: 'Posts can have at most 5000 characters' });
  });

  protected publish(): void {
    submit(this.form, async () => {
      const draft = this.newPost();

      this.publishing.set(true);

      const published = await firstValueFrom(
        this.postApi.publish({
          content: draft.content.trim(),
          visibility: draft.visibility,
        }),
      )
        .then(() => true)
        .catch(() => false);

      this.publishing.set(false);

      if (!published) {
        return;
      }

      this.form().reset({ content: '', visibility: draft.visibility });
      this.notifier.success('Post published');
      this.scrollToTop();
    });
  }

  protected changePage(event: PageEvent): void {
    this.size.set(event.pageSize);
    this.page.set(event.pageIndex);
  }

  protected onFriendshipAccepted(): void {
    this.scrollToTop();
  }

  protected onRemoved(): void {
    const { number, totalElements, size } = this.feed.value().page;
    const lastPage = Math.max(0, Math.ceil((totalElements - 1) / size) - 1);

    if (number > lastPage) {
      this.page.set(lastPage);
      return;
    }

    this.feed.reload();
  }

  private scrollToTop(): void {
    if (this.page() === 0) {
      this.feed.reload();
      return;
    }

    this.page.set(0);
  }
}
