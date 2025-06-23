<template>
  <div id="app">
    <LoginForm 
      v-if="!isAuthenticated" 
      @login-success="handleLoginSuccess" 
    />
    <div v-else class="app-container">
      <div class="app-header">
        <h1>Shared Checklist</h1>
        <div class="auth-info">
          <span>Logged in as: <strong>{{ currentUser }}</strong></span>
          <button @click="handleLogout" class="logout-button">Logout</button>
        </div>
      </div>
      <SharedChecklist ref="checklistComponent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import LoginForm from './components/LoginForm.vue';
import SharedChecklist from './components/SharedChecklist.vue';
import { isAuthenticated as checkAuth, getCurrentUsername, clearAuth } from './services/api';

const isAuthenticated = ref(false);
const currentUser = ref<string | null>(null);
const checklistComponent = ref<any>(null);

const updateAuthState = () => {
  isAuthenticated.value = checkAuth();
  currentUser.value = getCurrentUsername();
};

const handleLoginSuccess = async () => {
  console.log('Login success - updating auth state');
  updateAuthState();
  
  console.log('Current user after auth update:', currentUser.value);
  console.log('Checklist component ref:', checklistComponent.value);
  
  // Wait for next tick to ensure component is fully mounted
  await new Promise(resolve => setTimeout(resolve, 100));
  
  // Initialize the checklist component after successful login
  if (checklistComponent.value) {
    console.log('Calling initializeData on checklist component');
    await checklistComponent.value.initializeData();
  } else {
    console.error('Checklist component ref is null!');
  }
};

const handleLogout = () => {
  clearAuth();
  updateAuthState();
};

// Listen for auth required events (from API interceptor)
const handleAuthRequired = () => {
  updateAuthState();
};

onMounted(() => {
  updateAuthState();
  window.addEventListener('auth-required', handleAuthRequired);
  
  // Don't try to load user data on mount - wait for explicit login
  // This prevents browser auth dialogs on page load
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
  padding: 20px 0;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.app-header h1 {
  color: #333;
  margin: 0;
}

.auth-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.auth-info span {
  color: #666;
  font-size: 14px;
}

.logout-button {
  padding: 8px 16px;
  background: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.logout-button:hover {
  background: #c82333;
}
</style> 