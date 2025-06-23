<template>
  <div class="user-switcher">
    <label for="user-select">Switch User (Dev):</label>
    <select id="user-select" v-model="selectedUser" @change="switchUser">
      <option value="alice">Alice</option>
      <option value="bob">Bob</option>
      <option value="carol">Carol</option>
      <option value="admin">Admin</option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const emit = defineEmits<{
  userChanged: [username: string]
}>();

const selectedUser = ref('alice');

const switchUser = () => {
  // Store selection in localStorage
  localStorage.setItem('dev-selected-user', selectedUser.value);
  
  // Emit the change event
  emit('userChanged', selectedUser.value);
};

onMounted(() => {
  // Load saved selection
  const saved = localStorage.getItem('dev-selected-user');
  if (saved) {
    selectedUser.value = saved;
    // Don't emit on mount to avoid infinite loops
    // The parent will handle initial user loading
  }
});

// Expose a method to set the current user from parent
const setCurrentUser = (username: string) => {
  selectedUser.value = username;
  localStorage.setItem('dev-selected-user', username);
};

defineExpose({
  setCurrentUser
});
</script>

<style scoped>
.user-switcher {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #e3f2fd;
  border-radius: 6px;
  border: 1px solid #bbdefb;
  margin-bottom: 10px;
}

.user-switcher label {
  font-weight: 600;
  color: #1565c0;
  font-size: 14px;
  white-space: nowrap;
}

.user-switcher select {
  padding: 6px 10px;
  border: 1px solid #90caf9;
  border-radius: 4px;
  background: white;
  color: #1565c0;
  font-weight: 500;
}

.user-switcher select:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2);
}
</style> 