import { HttpClient, httpResource } from '@angular/common/http';
import { Service, Signal, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, emptyPage } from '../models/page';
import { Profile } from '../models/profile';
import { UpdateProfile, NewUser, User } from '../models/user';

@Service()
export class UserApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/users`;

  register(data: NewUser): Observable<User> {
    return this.http.post<User>(this.base, data);
  }

  profileById(userId: Signal<number>) {
    return httpResource<Profile>(() => `${this.base}/${userId()}`);
  }

  updateProfile(data: UpdateProfile): Observable<User> {
    return this.http.put<User>(`${this.base}/me`, data);
  }

  search(search: Signal<string>, page: Signal<number>, size: Signal<number>) {
    return httpResource<Page<Profile>>(
      () => ({
        url: this.base,
        params: { search: search(), page: page(), size: size() },
      }),
      { defaultValue: emptyPage<Profile>(size()) },
    );
  }
}
