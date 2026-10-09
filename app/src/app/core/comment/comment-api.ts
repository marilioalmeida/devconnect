import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PostComment, NewComment } from '../models/comment';
import { Page, emptyPage } from '../models/page';

@Service()
export class CommentApi {
  private readonly http = inject(HttpClient);

  postComments(postId: Signal<number>, quantity: Signal<number>) {
    return httpResource<Page<PostComment>>(
      () => ({
        url: this.urlOf(postId()),
        params: { page: 0, size: quantity() },
      }),
      { defaultValue: emptyPage<PostComment>(quantity()) },
    );
  }

  create(postId: number, comment: NewComment): Observable<PostComment> {
    return this.http.post<PostComment>(this.urlOf(postId), comment);
  }

  remove(postId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(`${this.urlOf(postId)}/${commentId}`);
  }

  private urlOf(postId: number): string {
    return `${environment.apiUrl}/posts/${postId}/comments`;
  }
}
