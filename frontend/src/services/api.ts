import axios from 'axios';
import type { ChecklistItem, ChecklistSummary } from '../types/api';

// Create axios instance with default config
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  withCredentials: true, // Include credentials for CORS
});

// Auth state
let currentAuth: { username: string; password: string } | null = null;

// Set authentication
export const setAuth = (username: string, password: string) => {
  currentAuth = { username, password };
  const credentials = btoa(`${username}:${password}`);
  api.defaults.headers.common['Authorization'] = `Basic ${credentials}`;
};

// Clear authentication
export const clearAuth = () => {
  currentAuth = null;
  delete api.defaults.headers.common['Authorization'];
};

// Get current auth status
export const isAuthenticated = () => {
  return currentAuth !== null;
};

// Get current username
export const getCurrentUsername = () => {
  return currentAuth?.username || null;
};

// Request interceptor to ensure auth header is always present
api.interceptors.request.use(
  (config) => {
    if (currentAuth && !config.headers['Authorization']) {
      const credentials = btoa(`${currentAuth.username}:${currentAuth.password}`);
      config.headers['Authorization'] = `Basic ${credentials}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
      // Prevent browser basic auth dialog by suppressing the error
      const suppressedError = new Error('Authentication required');
      suppressedError.name = 'AuthenticationError';
      window.dispatchEvent(new CustomEvent('auth-required'));
      return Promise.reject(suppressedError);
    }
    return Promise.reject(error);
  }
);

export const checklistApi = {
  async testAuth(): Promise<boolean> {
    try {
      await api.get('/user/current');
      return true;
    } catch (error) {
      return false;
    }
  },

  async getCurrentUser(): Promise<any> {
    const response = await api.get('/user/current');
    return response.data;
  },

  async getAllUsers(): Promise<any[]> {
    const response = await api.get('/user');
    return response.data;
  },

  async getAllChecklists(): Promise<ChecklistSummary[]> {
    const response = await api.get('/checklists');
    return response.data;
  },

  async getChecklist(checklistId: number): Promise<ChecklistItem[]> {
    const response = await api.get(`/checklists/${checklistId}`);
    return response.data;
  },

  async toggleItem(checklistId: number, itemId: number, userId: string): Promise<void> {
    await api.post(`/checklists/${checklistId}/item/${itemId}/user/${userId}/toggle`);
  },
};

export default api; 