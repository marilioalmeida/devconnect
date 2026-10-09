import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LikeSummary } from '../models/like';
import { Page, emptyPage } from '../models/page';
import { UserSummary } from '../models/user';

@Service()
export class LikeApi {
  private readonly http = inject(HttpClient);

  likers(postId: Signal<number>, quantity: Signal<number>) {
    return httpResource<Page<UserSummary>>(
      () => ({
        url: this.urlOf(postId()),
        params: { page: 0, size: quantity() },
      }),
      { defaultValue: emptyPage<UserSummary>(quantity()) },
    );
  }

  like(postId: number): Observable<LikeSummary> {
    return this.http.post<LikeSummary>(this.urlOf(postId), null);
  }

  unlike(postId: number): Observable<LikeSummary> {
    return this.http.delete<LikeSummary>(this.urlOf(postId));
  }

  private urlOf(postId: number): string {
    return `${environment.apiUrl}/posts/${postId}/likes`;
  }
}
