// Account-related types
export interface LoginRequest {
  userId: string;
  password: string;
}

// Login response matching backend AuthResource format
export interface LoginResponse {
  token: string;        // JWT access token
  tokenType: string;    // "Bearer"
  expiresIn: number;    // Seconds until expiry (3600)
  userId: string;       // User ID
}

export interface AccountSummary {
  accountId: number;
  userId: string;
  fullName: string;
  loginCount: number;
  lastLogin: string;
}

export interface AccountResponse {
  id: number;
  userId: string;
  balance: number;
  openBalance: number;
  loginCount: number;
  logoutCount: number;
  lastLogin: string;
  creationDate: string;
  profile: ProfileResponse;
}

export interface ProfileResponse {
  userId: string;
  fullName: string;
  email: string;
  address: string;
  creditCard: string;
  createdAt: string;
  updatedAt: string;
}

export interface RegisterRequest {
  userId: string;
  password: string;
  fullName: string;
  email: string;
  address?: string;
  creditCard?: string;
  openBalance?: number;
}

export interface UpdateProfileRequest {
  fullName?: string;
  email?: string;
  address?: string;
  creditCard?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

