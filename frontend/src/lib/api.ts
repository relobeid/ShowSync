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
};

export default api; 