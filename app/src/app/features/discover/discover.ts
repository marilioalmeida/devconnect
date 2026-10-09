import { Component, computed, inject, linkedSignal, signal } from '@angular/core';
import { FormField, debounce, form } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { firstValueFrom } from 'rxjs';
import { FriendshipApi } from '../../core/friendship/friendship-api';
import { Profile, RelationshipStatus } from '../../core/models/profile';
import { Notifier } from '../../core/notification/notifier';
import { UserApi } from '../../core/user/user-api';
import { UserRow } from '../../shared/user-row/user-row';
import { LoadError } from '../../shared/load-error/load-error';

const DEFAULT_PAGE_SIZE = 10;

const BADGE_BY_STATUS: Record<RelationshipStatus, string> = {
  NONE: '',
  REQUEST_SENT: 'Request sent',
  REQUEST_RECEIVED: 'Request received',
  FRIENDS: 'Friends',
  OWN_PROFILE: 'You',
};

@Component({
  selector: 'app-discover',
  imports: [
    FormField,
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
  templateUrl: './discover.html',
  styleUrl: './discover.scss',
})
export class Discover {
  private readonly userApi = inject(UserApi);
  private readonly friendshipApi = inject(FriendshipApi);
  private readonly notifier = inject(Notifier);

  protected readonly size = signal(DEFAULT_PAGE_SIZE);
  protected readonly pageSizeOptions = [5, 10, 20];
  protected readonly requestingFor = signal<number | null>(null);

  protected readonly searchModel = signal({ search: '' });

  protected readonly form = form(this.searchModel, (path) => {
    debounce(path.search, 400);
  });

  private readonly search = computed(() => this.searchModel().search.trim());

  protected readonly page = linkedSignal({
    source: this.search,
    computation: () => 0,
  });

  protected readonly results = this.userApi.search(this.search, this.page, this.size);

  protected badgeOf(profile: Profile): string {
    return BADGE_BY_STATUS[profile.relationship.status];
  }

  protected canRequest(profile: Profile): boolean {
    return profile.relationship.status === 'NONE';
  }

  protected async sendRequest(profile: Profile): Promise<void> {
    this.requestingFor.set(profile.id);

    const sent = await firstValueFrom(this.friendshipApi.sendRequest(profile.id))
      .then(() => true)
      .catch(() => false);

    this.requestingFor.set(null);

    if (sent) {
      this.notifier.success(`Friend request sent to ${profile.fullName}`);
      this.results.reload();
    }
  }

  protected changePage(event: PageEvent): void {
    this.size.set(event.pageSize);
    this.page.set(event.pageIndex);
  }
}
