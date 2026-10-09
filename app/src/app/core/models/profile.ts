export type RelationshipStatus =
  | 'NONE'
  | 'REQUEST_SENT'
  | 'REQUEST_RECEIVED'
  | 'FRIENDS'
  | 'OWN_PROFILE';

export interface Relationship {
  status: RelationshipStatus;
  friendshipId: number | null;
}

export interface Profile {
  id: number;
  fullName: string;
  nickname: string | null;
  profileImage: string | null;
  relationship: Relationship;
}
