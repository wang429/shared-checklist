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
      <CreateChecklistForm @checklist-created="onChecklistCreated" />
    </div>

    <div v-if="loading" class="loading">Loading checklist...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    
    <div v-else class="checklist-container">
      <div class="users-header">
        <div class="item-label">Task</div>
        <div class="user-columns">
          <div 
            v-for="(user, index) in orderedUsers" 
            :key="user.id" 
            class="user-column"
            :class="{ 'current-user-column': index === 0 && currentUser && user.id === currentUser.id }"
          >
            {{ user.username }}
            <span v-if="index === 0 && currentUser && user.id === currentUser.id" class="current-badge">YOU</span>
          </div>
        </div>
      </div>

      <div v-for="item in checklistItems" :key="item.id" class="checklist-item">
        <div class="item-content">{{ item.content }}</div>
        <div class="user-progress">
          <div v-for="user in orderedUsers" :key="user.id" class="user-checkbox">
            <!-- Show checkbox only for current user -->
            <input 
              v-if="currentUser && user.id === currentUser.id"
              type="checkbox"
              :checked="item.progress[user.username] || false"
              @change="toggleItem(item.id, user.id)"
              class="current-user-checkbox"
            />
            <!-- Show progress indicator for all users -->
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
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { checklistApi } from '../services/api';
import type { ChecklistItem, ChecklistSummary, User } from '../types/api';
import CreateChecklistForm from './CreateChecklistForm.vue';

const checklistId = ref<number | null>(null);
const availableChecklists = ref<ChecklistSummary[]>([]);
const currentUser = ref<User | null>(null);
const currentUserName = ref('');
const checklistItems = ref<ChecklistItem[]>([]);
const loading = ref(false);
const error = ref('');

// All users for displaying progress - loaded from backend
const users = ref<User[]>([]);

// Computed property to show current user first
const orderedUsers = computed(() => {
  if (!currentUser.value || users.value.length === 0) {
    return users.value;
  }
  
  // Find current user and other users
  const currentUserIndex = users.value.findIndex(user => user.id === currentUser.value!.id);
  if (currentUserIndex === -1) {
    return users.value; // Current user not found, return original order
  }
  
  // Put current user first, then the rest
  const reorderedUsers = [...users.value];
  const currentUserData = reorderedUsers.splice(currentUserIndex, 1)[0];
  return [currentUserData, ...reorderedUsers];
});

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
    
    // In dev mode, if we can't get current user, ensure we have a default dev user set
    const savedDevUser = localStorage.getItem('dev-selected-user');
    if (!savedDevUser) {
      localStorage.setItem('dev-selected-user', 'alice');
      // Retry after setting default user
      setTimeout(() => loadCurrentUser(), 100);
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
    const response = await checklistApi.getAllUsers();
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

// Function to handle when a new checklist is created
const onChecklistCreated = async (newChecklistId: number) => {
  try {
    // Refresh the available checklists
    await loadChecklists();
    
    // Select the new checklist
    checklistId.value = newChecklistId;
    
    // Load the new checklist
    await loadChecklist();
  } catch (err) {
    error.value = 'Failed to load the new checklist. Please refresh the page.';
    console.error('Error loading new checklist:', err);
  }
};

// Function to initialize data after authentication
const initializeData = async () => {
  try {
    await loadCurrentUser();
    
    if (currentUser.value) {
      // Load available checklists and all users
      await Promise.all([loadChecklists(), loadUsers()]);
      
      // Load the checklist once we have everything
      if (checklistId.value) {
        await loadChecklist();
      }
    }
  } catch (error) {
    console.error('Error in initializeData:', error);
  }
};

// Expose the initialize function for parent component to call
defineExpose({
  initializeData
});

// Watch for when we need to reinitialize data
watch(
  () => [currentUser.value, availableChecklists.value.length],
  async ([user, checklistCount]) => {
    // If we have a user but no checklists, try to load data
    if (user && checklistCount === 0) {
      await nextTick();
      await loadChecklists();
      await loadUsers();
      if (checklistId.value) {
        await loadChecklist();
      }
    }
  },
  { immediate: true }
);

onMounted(async () => {
  // Try to initialize data immediately if we don't have current user
  if (!currentUser.value) {
    await initializeData();
  }
});
</script>

<style scoped>
.shared-checklist {
  max-width: 800px;
  margin: 0 auto;
  padding: 10px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.selector-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 15px;
  margin-bottom: 15px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  flex-wrap: wrap;
}

.checklist-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checklist-selector label {
  font-weight: 600;
  color: #555;
  white-space: nowrap;
}

.checklist-selector select {
  padding: 6px 10px;
  border: 2px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  min-width: 150px;
}

.loading, .error {
  text-align: center;
  padding: 15px;
  margin: 15px 0;
  border-radius: 6px;
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
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.users-header {
  display: flex;
  background: #f5f5f5;
  padding: 10px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}

.item-label {
  flex: 1;
  color: #333;
  padding: 6px 8px;
  margin: 0 1px;
  text-align: left;
}

.user-columns {
  display: flex;
  min-width: 300px;
}

.user-column {
  flex: 1;
  text-align: center;
  color: #555;
  padding: 6px 8px;
  position: relative;
  margin: 0 1px;
}

.current-user-column {
  background: #e3f2fd;
  color: #1565c0;
  font-weight: bold;
  border-radius: 3px;
}

.current-badge {
  display: block;
  font-size: 9px;
  font-weight: 600;
  color: #1976d2;
  margin-top: 1px;
  letter-spacing: 0.5px;
}

.checklist-item {
  display: flex;
  align-items: center;
  padding: 10px;
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
  font-size: 15px;
  padding: 6px 8px;
  margin: 0 1px;
  text-align: left;
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
  padding: 6px 8px;
  margin: 0 1px;
}

.current-user-checkbox {
  margin-right: 6px;
  transform: scale(1.1);
  cursor: pointer;
}

.progress-indicator {
  font-size: 16px;
  font-weight: bold;
}

.progress-indicator.completed {
  color: #4caf50;
}

.progress-indicator.current-user {
  background: #e3f2fd;
  padding: 3px 6px;
  border-radius: 3px;
}

.current-user {
  background: #e8f5e8;
  border-radius: 3px;
  padding: 1px;
}
</style> 