import { UserSummary } from './user';

export interface PostComment {
  id: number;
  content: string;
  createdAt: string;
  author: UserSummary;
}

export interface NewComment {
  content: string;
}
