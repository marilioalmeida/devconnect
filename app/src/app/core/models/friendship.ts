import { UserSummary } from './user';

export type FriendshipStatus = 'PENDING' | 'ACCEPTED';

export interface FriendRequest {
  id: number;
  requestedAt: string;
  requester: UserSummary;
}

export interface Friend {
  id: number;
  friendsSince: string;
  friend: UserSummary;
}

export interface Friendship {
  id: number;
  status: FriendshipStatus;
  requestedAt: string;
  respondedAt: string | null;
  requester: UserSummary;
  recipient: UserSummary;
}
