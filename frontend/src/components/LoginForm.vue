<template>
  <div class="login-overlay">
    <div class="login-form">
      <h2>Login Required</h2>
      <p class="login-description">Please enter your credentials to access the shared checklist.</p>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username:</label>
          <input
            id="username"
            v-model="username"
            type="text"
            required
            placeholder="Enter username"
            :disabled="loading"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password:</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            placeholder="Enter password"
            :disabled="loading"
          />
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <button type="submit" :disabled="loading" class="login-button">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
      
      <div class="demo-credentials">
        <h4>Demo Credentials:</h4>
        <p><strong>Username:</strong> alice</p>
        <p><strong>Password:</strong> password</p>
        <button @click="useDemo" class="demo-button" :disabled="loading">
          Use Demo Credentials
        </button>
        <div class="other-users">
          <p><small>Other users: bob, carol, admin (all use "password")</small></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { setAuth, checklistApi } from '../services/api';

const emit = defineEmits<{
  loginSuccess: []
}>();

const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');

const handleLogin = async () => {
  if (!username.value || !password.value) {
    error.value = 'Please enter both username and password';
    return;
  }
  
  loading.value = true;
  error.value = '';
  
  try {
    // Set auth credentials
    setAuth(username.value, password.value);
    
    // Test the authentication
    const isValid = await checklistApi.testAuth();
    
    if (isValid) {
      emit('loginSuccess');
    } else {
      error.value = 'Invalid credentials. Please try again.';
      setAuth('', ''); // Clear invalid auth
    }
  } catch (err) {
    error.value = 'Login failed. Please check your credentials.';
    setAuth('', ''); // Clear invalid auth
  } finally {
    loading.value = false;
  }
};

const useDemo = () => {
  username.value = 'alice';
  password.value = 'password';
  handleLogin();
};
</script>

<style scoped>
.login-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.login-form {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 400px;
  margin: 20px;
}

.login-form h2 {
  color: #333;
  margin-bottom: 10px;
  text-align: center;
}

.login-description {
  color: #666;
  margin-bottom: 25px;
  text-align: center;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
}

.form-group input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  background: #fff5f5;
  color: #cc0000;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
  border: 1px solid #ffcdd2;
  font-size: 14px;
}

.login-button {
  width: 100%;
  padding: 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-button:hover:not(:disabled) {
  background: #0056b3;
}

.login-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.demo-credentials {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: center;
}

.demo-credentials h4 {
  color: #666;
  margin-bottom: 10px;
  font-size: 14px;
}

.demo-credentials p {
  margin: 5px 0;
  font-size: 13px;
  color: #777;
}

.demo-button {
  margin-top: 10px;
  padding: 8px 16px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.demo-button:hover:not(:disabled) {
  background: #218838;
}

.demo-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.other-users {
  margin-top: 10px;
}

.other-users p {
  color: #888;
  font-size: 12px;
  margin: 0;
}
</style> 