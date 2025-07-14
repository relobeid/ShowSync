// API Response Types
export interface ApiResponse<T = unknown> {
  data?: T;
  message?: string;
  error?: string;
  status?: number;
}

// Authentication Types
export interface AuthRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  displayName: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  role: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  displayName: string;
  profilePictureUrl?: string;
  bio?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Media Types
export interface Media {
  id: number;
  type: 'MOVIE' | 'TV_SHOW' | 'BOOK';
  title: string;
  originalTitle?: string;
  description?: string;
  releaseDate?: string;
  posterUrl?: string;
  backdropUrl?: string;
  externalId?: string;
  externalSource?: string;
  averageRating?: number;
  ratingCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface MediaSearchResponse {
  results: Media[];
  totalResults: number;
  page: number;
  totalPages: number;
}

// User Media Library Types
export interface UserMediaInteraction {
  id: number;
  userId: number;
  mediaId: number;
  rating?: number;
  status: 'WATCHING' | 'COMPLETED' | 'PLAN_TO_WATCH' | 'DROPPED' | 'ON_HOLD';
  review?: string;
  progress?: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
  media?: Media;
}

export interface AddMediaToLibraryRequest {
  mediaId: number;
  status?: string;
  rating?: number;
}

// Review Types
export interface Review {
  id: number;
  userId: number;
  mediaId: number;
  title?: string;
  content: string;
  rating?: number;
  helpfulVotes: number;
  totalVotes: number;
  isSpoiler: boolean;
  isModerated: boolean;
  moderationReason?: string;
  createdAt: string;
  updatedAt: string;
  user?: User;
}

export interface CreateReviewRequest {
  mediaId: number;
  title?: string;
  content: string;
  rating?: number;
  isSpoiler?: boolean;
}

// Group Types
export interface Group {
  id: number;
  name: string;
  description?: string;
  privacySetting: 'PUBLIC' | 'PRIVATE';
  createdBy: number;
  maxMembers: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  memberCount?: number;
  isMember?: boolean;
  userRole?: string;
}

export interface CreateGroupRequest {
  name: string;
  description?: string;
  privacySetting?: 'PUBLIC' | 'PRIVATE';
}

// Health Check Types
export interface HealthResponse {
  status: 'UP' | 'DOWN';
  components?: {
    db?: { status: 'UP' | 'DOWN' };
    redis?: { status: 'UP' | 'DOWN' };
  };
} 