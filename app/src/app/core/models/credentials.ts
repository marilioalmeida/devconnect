export interface Credentials {
  email: string;
  password: string;
}

export interface AccessToken {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}
