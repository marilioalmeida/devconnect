import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Auth } from '../auth/auth';
import { Friend, Friendship, FriendRequest } from '../models/friendship';
import { Page, emptyPage } from '../models/page';

@Service()
export class FriendshipApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(Auth);
  private readonly base = `${environment.apiUrl}/friendships`;

  receivedRequests() {
    return httpResource<FriendRequest[]>(
      () => (this.auth.token() === null ? undefined : `${this.base}/requests`),
      { defaultValue: [] },
    );
  }

  pagedFriends(search: Signal<string>, page: Signal<number>, size: Signal<number>) {
    return httpResource<Page<Friend>>(
      () => ({
        url: this.base,
        params: { search: search(), page: page(), size: size() },
      }),
      { defaultValue: emptyPage<Friend>(size()) },
    );
  }

  sendRequest(recipientId: number): Observable<Friendship> {
    return this.http.post<Friendship>(this.base, { recipientId });
  }

  accept(friendshipId: number): Observable<Friendship> {
    return this.http.patch<Friendship>(`${this.base}/${friendshipId}/accept`, null);
  }

  remove(friendshipId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${friendshipId}`);
  }
}
