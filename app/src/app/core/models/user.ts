export interface UserSummary {
  id: number;
  fullName: string;
  nickname: string | null;
  profileImage: string | null;
}

export interface User {
  id: number;
  fullName: string;
  email: string;
  nickname: string | null;
  birthDate: string;
  profileImage: string | null;
  active: boolean;
}

export interface UpdateProfile {
  fullName: string;
  nickname: string | null;
  profileImage: string | null;
}

export interface NewUser {
  fullName: string;
  email: string;
  nickname: string | null;
  birthDate: string;
  password: string;
  profileImage: string | null;
}
