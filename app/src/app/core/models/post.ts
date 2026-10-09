import { UserSummary } from './user';

export type Visibility = 'PUBLIC' | 'PRIVATE';

export interface Post {
  id: number;
  content: string;
  createdAt: string;
  visibility: Visibility;
  author: UserSummary;
  likeCount: number;
  likedByCurrentUser: boolean;
  commentCount: number;
}

export interface NewPost {
  content: string;
  visibility: Visibility;
}
