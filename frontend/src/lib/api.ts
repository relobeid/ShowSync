import { AuthRequest, AuthResponse, RegisterRequest, User } from '@/types/api';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * API client for ShowSync backend
 */
export const api = {
  /**
   * Base fetch wrapper with error handling
   */
  async fetch(endpoint: string, options: RequestInit = {}) {
    const url = `${API_BASE}${endpoint}`;
    
    try {
      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP error! status: ${response.status}`);
      }

      return response;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  },

  /**
   * GET request
   */
  async get(endpoint: string, token?: string) {
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await this.fetch(endpoint, { method: 'GET', headers });
    return response.json();
  },

  /**
   * POST request
   */
  async post(endpoint: string, data: unknown, token?: string) {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await this.fetch(endpoint, {
      method: 'POST',
      headers,
      body: JSON.stringify(data),
    });
    return response.json();
  },

  /**
   * PUT request
   */
  async put(endpoint: string, data: unknown, token?: string) {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await this.fetch(endpoint, {
      method: 'PUT',
      headers,
      body: JSON.stringify(data),
    });
    return response.json();
  },

  /**
   * DELETE request
   */
  async delete(endpoint: string, token?: string) {
    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await this.fetch(endpoint, { method: 'DELETE', headers });
    return response.json();
  },

  // Authentication specific methods
  auth: {
    /**
     * Login user
     */
    async login(credentials: AuthRequest): Promise<AuthResponse> {
      const response = await api.post('/api/auth/login', credentials);
      return response;
    },

    /**
     * Register new user
     */
    async register(userData: RegisterRequest): Promise<AuthResponse> {
      const response = await api.post('/api/auth/register', userData);
      return response;
    },

    /**
     * Get current user profile
     */
    async getProfile(token: string): Promise<User> {
      const response = await api.get('/api/auth/profile', token);
      return response;
    },

    /**
     * Update user profile
     */
    async updateProfile(profileData: Partial<User>, token: string): Promise<User> {
      const response = await api.put('/api/auth/profile', profileData, token);
      return response;
    },
  },

  // Health check
  health: {
    async check() {
      const response = await api.get('/api/health');
      return response;
    },
  },

  // Recommendations API
  recommendations: {
    async getPersonal(page = 0, size = 20, token?: string) {
      return api.get(`/api/recommendations/personal?page=${page}&size=${size}`, token);
    },
    async getRealtime({ mediaId, limit = 10 }: { mediaId?: number; limit?: number }, token?: string) {
      const q = mediaId ? `?mediaId=${mediaId}&limit=${limit}` : `?limit=${limit}`;
      return api.get(`/api/recommendations/realtime${q}`, token);
    },
    async getTrending(limit = 10, token?: string) {
      return api.get(`/api/recommendations/trending?limit=${limit}`, token);
    },
    async getGroupRecommendations(page = 0, size = 20, token?: string) {
      return api.get(`/api/recommendations/groups?page=${page}&size=${size}`, token);
    },
    async getGroupContent(groupId: number, page = 0, size = 20, token?: string) {
      return api.get(`/api/recommendations/groups/${groupId}/content?page=${page}&size=${size}`, token);
    },
    async markViewed(type: 'CONTENT' | 'GROUP', id: number, token?: string) {
      return api.post(`/api/recommendations/view/${type}/${id}`, {}, token);
    },
    async dismiss(type: 'CONTENT' | 'GROUP', id: number, reason?: string, token?: string) {
      const q = reason ? `?reason=${encodeURIComponent(reason)}` : '';
      return api.post(`/api/recommendations/dismiss/${type}/${id}${q}`, {}, token);
    },
    async feedback(type: 'CONTENT' | 'GROUP', id: number, rating: number, comment?: string, token?: string) {
      const q = `?rating=${rating}` + (comment ? `&comment=${encodeURIComponent(comment)}` : '');
      return api.post(`/api/recommendations/feedback/${type}/${id}${q}`, {}, token);
    },
    async generateAll() {
      return api.post('/api/recommendations/generate', {});
    },
    async generateMe(token: string) {
      return api.post('/api/recommendations/generate/me', {}, token);
    },
    async analytics(days = 30, token?: string) {
      return api.get(`/api/recommendations/analytics?days=${days}`, token);
    },
    async myInsights(token: string) {
      return api.get('/api/recommendations/insights/me', token);
    },
    async mySummary(token: string) {
      return api.get('/api/recommendations/summary/me', token);
    },
    async similar(mediaId: number, limit = 10, token?: string) {
      return api.get(`/api/recommendations/similar/${mediaId}?limit=${limit}`, token);
    },
    async byType(type: string, limit = 10, token?: string) {
      return api.get(`/api/recommendations/by-type?type=${encodeURIComponent(type)}&limit=${limit}`, token);
    },
  },
};

export default api; 