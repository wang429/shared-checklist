<template>
  <div class="shared-checklist">
    <div class="selector-container">
      <div class="checklist-selector">
        <label for="checklist-select">Checklist:</label>
        <select id="checklist-select" v-model="checklistId" @change="loadChecklist">
          <option v-for="checklist in availableChecklists" :key="checklist.id" :value="checklist.id">
            {{ checklist.name }}
          </option>
        </select>
      </div>
      <div class="current-user-info">
        <span class="current-user-label">Logged in as:</span>
        <span class="current-user-name">{{ currentUserName }}</span>
      </div>
    </div>

    <div v-if="loading" class="loading">Loading checklist...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="checklist-container">
      <div class="users-header">
        <div class="item-label">Task</div>
        <div class="user-columns">
          <div v-for="user in users" :key="user.id" class="user-column">
            {{ user.username }}
          </div>
        </div>
      </div>

      <div v-for="item in checklistItems" :key="item.id" class="checklist-item">
        <div class="item-content">{{ item.content }}</div>
        <div class="user-progress">
          <div v-for="user in users" :key="user.id" class="user-checkbox">
            <input 
              type="checkbox"
              :checked="item.progress[user.username] || false"
              :disabled="!currentUser || user.id !== currentUser.id"
              @change="toggleItem(item.id, user.id)"
              :class="{ 'current-user': currentUser && user.id === currentUser.id }"
            />
            <span class="progress-indicator" :class="{ 
              'completed': item.progress[user.username], 
              'current-user': currentUser && user.id === currentUser.id 
            }">
              {{ item.progress[user.username] ? '✓' : '○' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { checklistApi } from '../services/api';
import type { ChecklistItem, ChecklistSummary, User } from '../types/api';

const checklistId = ref<number | null>(null);
const availableChecklists = ref<ChecklistSummary[]>([]);
const currentUser = ref<User | null>(null);
const currentUserName = ref('');
const checklistItems = ref<ChecklistItem[]>([]);
const loading = ref(false);
const error = ref('');

// All users for displaying progress - loaded from backend
const users = ref<User[]>([]);

const loadCurrentUser = async () => {
  try {
    currentUser.value = await checklistApi.getCurrentUser();
    currentUserName.value = currentUser.value.username;
  } catch (err) {
    // Silently fail if not authenticated - this prevents browser auth dialogs
    console.error('Error loading current user:', err);
    if (err.name !== 'AuthenticationError') {
      error.value = 'Failed to load current user. Please try again.';
    }
  }
};

const loadChecklists = async () => {
  try {
    availableChecklists.value = await checklistApi.getAllChecklists();
    // Set default checklist to first one if none selected
    if (!checklistId.value && availableChecklists.value.length > 0) {
      checklistId.value = availableChecklists.value[0].id;
    }
  } catch (err) {
    error.value = 'Failed to load checklists. Please try again.';
    console.error('Error loading checklists:', err);
  }
};

const loadUsers = async () => {
  try {
    console.log('Making API call to get all users...');
    const response = await checklistApi.getAllUsers();
    console.log('getAllUsers API response:', response);
    users.value = response;
  } catch (err) {
    console.error('Error loading users:', err);
    // Fallback to current user only
    if (currentUser.value) {
      users.value = [currentUser.value];
    }
  }
};

const loadChecklist = async () => {
  if (!currentUser.value || !checklistId.value) return;
  
  loading.value = true;
  error.value = '';
  
  try {
    checklistItems.value = await checklistApi.getChecklist(checklistId.value);
  } catch (err) {
    error.value = 'Failed to load checklist. Please try again.';
    console.error('Error loading checklist:', err);
  } finally {
    loading.value = false;
  }
};

const toggleItem = async (itemId: number, userId: string) => {
  if (!currentUser.value || !checklistId.value) return;
  
  // Only allow toggling if this is the current user's checkbox
  // In dev mode with user switching, currentUser reflects the selected dev user
  if (userId !== currentUser.value.id) {
    return;
  }
  
  // Optimistic update - update UI immediately for better UX
  const item = checklistItems.value.find(item => item.id === itemId);
  const username = currentUser.value.username;
  
  if (item && username) {
    const currentStatus = item.progress[username] || false;
    item.progress[username] = !currentStatus;
  }
  
  try {
    await checklistApi.toggleItem(checklistId.value, itemId, userId);
    // Only refresh if there was an error to revert optimistic update
  } catch (err) {
    // Revert optimistic update on error
    if (item && username) {
      item.progress[username] = !item.progress[username];
    }
    error.value = 'Failed to update item. Please try again.';
    console.error('Error toggling item:', err);
  }
};

// Function to initialize data after authentication
const initializeData = async () => {
  console.log('SharedChecklist: initializeData called');
  
  try {
    console.log('Loading current user...');
    await loadCurrentUser();
    console.log('Current user loaded:', currentUser.value);
    
    if (currentUser.value) {
      console.log('Loading checklists and users...');
      // Load available checklists and all users
      await Promise.all([loadChecklists(), loadUsers()]);
      console.log('Checklists loaded:', availableChecklists.value);
      console.log('Users loaded:', users.value);
      
      // Load the checklist once we have everything
      if (checklistId.value) {
        console.log('Loading checklist with ID:', checklistId.value);
        await loadChecklist();
      } else {
        console.log('No checklist ID selected');
      }
    } else {
      console.error('No current user found after loadCurrentUser');
    }
  } catch (error) {
    console.error('Error in initializeData:', error);
  }
};

// Expose the initialize function for parent component to call
defineExpose({
  initializeData
});

onMounted(() => {
  // Don't make any API calls on mount - wait for parent to trigger after auth
});
</script>

<style scoped>
.shared-checklist {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.selector-container {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-wrap: wrap;
}

.checklist-selector,
.current-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.checklist-selector label,
.current-user-label {
  font-weight: 600;
  color: #555;
  white-space: nowrap;
}

.checklist-selector select {
  padding: 8px 12px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: white;
  min-width: 150px;
}

.current-user-name {
  font-weight: bold;
  color: #007bff;
  background: #e3f2fd;
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #bbdefb;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  margin: 20px 0;
  border-radius: 8px;
}

.loading {
  background: #f0f8ff;
  color: #0066cc;
}

.error {
  background: #fff5f5;
  color: #cc0000;
  border: 1px solid #ffcdd2;
}

.checklist-container {
  background: white;
  border-Radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.users-header {
  display: flex;
  background: #f5f5f5;
  padding: 15px;
  font-weight: 600;
  border-bottom: 2px solid #e0e0e0;
}

.item-label {
  flex: 1;
  color: #333;
}

.user-columns {
  display: flex;
  min-width: 300px;
}

.user-column {
  flex: 1;
  text-align: center;
  color: #555;
  padding: 0 10px;
}

.checklist-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.checklist-item:hover {
  background: #fafafa;
}

.checklist-item:last-child {
  border-bottom: none;
}

.item-content {
  flex: 1;
  color: #333;
  font-size: 16px;
}

.user-progress {
  display: flex;
  min-width: 300px;
}

.user-checkbox {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  padding: 0 10px;
}

.user-checkbox input[type="checkbox"] {
  margin-right: 8px;
  transform: scale(1.2);
  cursor: pointer;
}

.user-checkbox input[type="checkbox"]:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.progress-indicator {
  font-size: 18px;
  font-weight: bold;
}

.progress-indicator.completed {
  color: #4caf50;
}

.progress-indicator.current-user {
  background: #e3f2fd;
  padding: 4px 8px;
  border-radius: 4px;
}

.current-user {
  background: #e8f5e8;
  border-radius: 4px;
  padding: 2px;
}
</style> 