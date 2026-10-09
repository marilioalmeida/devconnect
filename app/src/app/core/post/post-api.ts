import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, emptyPage } from '../models/page';
import { NewPost, Post, Visibility } from '../models/post';

@Service()
export class PostApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/posts`;

  pagedFeed(page: Signal<number>, size: Signal<number>) {
    return httpResource<Page<Post>>(
      () => ({
        url: `${this.base}/feed`,
        params: { page: page(), size: size() },
      }),
      { defaultValue: emptyPage<Post>(size()) },
    );
  }

  userPosts(userId: Signal<number>, page: Signal<number>, size: Signal<number>) {
    return httpResource<Page<Post>>(
      () => ({
        url: `${environment.apiUrl}/users/${userId()}/posts`,
        params: { page: page(), size: size() },
      }),
      { defaultValue: emptyPage<Post>(size()) },
    );
  }

  publish(post: NewPost): Observable<Post> {
    return this.http.post<Post>(this.base, post);
  }

  updateContent(postId: number, content: string): Observable<Post> {
    return this.http.patch<Post>(`${this.base}/${postId}`, { content });
  }

  updateVisibility(postId: number, visibility: Visibility): Observable<Post> {
    return this.http.patch<Post>(`${this.base}/${postId}/visibility`, { visibility });
  }

  remove(postId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${postId}`);
  }
}
