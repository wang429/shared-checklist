<template>
  <div id="app">
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading...</p>
    </div>
    <div v-else-if="!isAuthenticated" class="auth-required-container">
      <div class="auth-message">
        <h2>Authentication Required</h2>
        <p>Please authenticate to access the shared checklist.</p>
        <p><small>You will be redirected to the login page automatically.</small></p>
      </div>
    </div>
    <div v-else class="app-container">
      <div class="app-header">
        <h1>Shared Checklist</h1>
        <div class="auth-info">
          <DevUserSwitcher v-if="isDev" ref="userSwitcher" @user-changed="handleUserSwitch" />
          <span>Logged in as: <strong>{{ currentUser }}</strong></span>
          <button @click="handleLogout" class="logout-button" v-if="isDev">Logout</button>
        </div>
      </div>
      <SharedChecklist ref="checklistComponent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import SharedChecklist from './components/SharedChecklist.vue';
import DevUserSwitcher from './components/DevUserSwitcher.vue';
import { isAuthenticated as checkAuth, getCurrentUsername, clearAuth } from './services/api';

const isAuthenticated = ref(false);
const currentUser = ref<string | null>(null);
const checklistComponent = ref<any>(null);
const userSwitcher = ref<any>(null);
const loading = ref(true);
const isDev = ref(false);

const updateAuthState = async () => {
  loading.value = true;
  try {
    // In dev mode, ensure the stored user is set before making the API call
    const savedDevUser = localStorage.getItem('dev-selected-user');
    
    // Create headers object and include dev user header if available
    const headers = {};
    if (savedDevUser) {
      headers['X-Dev-User'] = savedDevUser;
    }
    
    const response = await fetch('/api/user/current', { headers });
    if (response.ok) {
      // No auth required (dev mode) or already authenticated
      isAuthenticated.value = true;
      const userData = await response.json();
      currentUser.value = userData.username;
      
      // Check if we're in dev mode by seeing if we got a user without auth
      isDev.value = !checkAuth();
      
      // In dev mode, sync the user switcher with the current user
      if (isDev.value && userSwitcher.value) {
        userSwitcher.value.setCurrentUser(userData.username);
      }
    } else if (response.status === 401) {
      // Auth required (production mode)
      isAuthenticated.value = false;
      // In production, redirect to login
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    } else {
      // Fallback to existing auth check
      isAuthenticated.value = checkAuth();
      currentUser.value = getCurrentUsername();
    }
  } catch (error) {
    // Fallback to existing auth check
    isAuthenticated.value = checkAuth();
    currentUser.value = getCurrentUsername();
  } finally {
    loading.value = false;
  }
};

const handleLogout = () => {
  if (isDev.value) {
    // In dev mode, just clear the auth state (though it will auto-login again)
    clearAuth();
    updateAuthState();
  } else {
    // In production, redirect to logout
    window.location.href = '/logout';
  }
};

const handleUserSwitch = async (username: string) => {
  // Update the auth state with the new user
  await updateAuthState();
  
  // Reload the checklist data with the new user context
  if (checklistComponent.value) {
    await checklistComponent.value.initializeData();
  }
};

// Listen for auth required events (from API interceptor)
const handleAuthRequired = () => {
  updateAuthState();
};

onMounted(async () => {
  await updateAuthState();
  window.addEventListener('auth-required', handleAuthRequired);
  
  // If authenticated, initialize the checklist
  if (isAuthenticated.value) {
    // Wait for component to be ready
    await new Promise(resolve => setTimeout(resolve, 100));
    if (checklistComponent.value) {
      await checklistComponent.value.initializeData();
    }
  }
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f8f9fa;
}

#app {
  min-height: 100vh;
}

.app-container {
  min-height: 100vh;
  padding: 10px 0;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  margin-bottom: 15px;
}

.app-header h1 {
  color: #333;
  margin: 0;
  font-size: 24px;
}

.auth-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auth-info span {
  color: #666;
  font-size: 14px;
}

.logout-button {
  padding: 6px 12px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.logout-button:hover {
  background: #c82333;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f8f9fa;
}

.loading-spinner {
  width: 35px;
  height: 35px;
  border: 3px solid #e3f2fd;
  border-top: 3px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p {
  margin-top: 15px;
  color: #666;
  font-size: 15px;
}

.auth-required-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f8f9fa;
}

.auth-message {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
  max-width: 400px;
}

.auth-message h2 {
  color: #333;
  margin-bottom: 8px;
}

.auth-message p {
  color: #666;
  margin: 8px 0;
}
</style> 