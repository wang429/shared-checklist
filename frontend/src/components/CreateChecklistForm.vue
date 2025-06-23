<template>
  <div class="create-checklist-form">
    <button @click="showForm = !showForm" class="create-button">
      {{ showForm ? 'Cancel' : '+ Create New Checklist' }}
    </button>
    
    <div v-if="showForm" class="form-container">
      <h3>Create New Checklist</h3>
      
      <div class="form-group">
        <label for="checklist-name">Checklist Name:</label>
        <input
          id="checklist-name"
          v-model="checklistName"
          type="text"
          placeholder="Enter checklist name"
          maxlength="100"
          required
        />
      </div>
      
      <div class="form-group">
        <label>Initial Items (one per line):</label>
        <textarea
          v-model="itemsText"
          placeholder="Enter items, one per line:
Buy groceries
Call dentist
Review documents"
          rows="6"
          maxlength="2000"
        ></textarea>
      </div>
      
      <div class="form-actions">
        <button @click="createChecklist" :disabled="!checklistName.trim() || loading" class="submit-button">
          {{ loading ? 'Creating...' : 'Create Checklist' }}
        </button>
        <button @click="resetForm" class="cancel-button">Clear</button>
      </div>
      
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { checklistApi } from '../services/api';

const emit = defineEmits<{
  checklistCreated: [checklistId: number]
}>();

const showForm = ref(false);
const checklistName = ref('');
const itemsText = ref('');
const loading = ref(false);
const error = ref('');

const createChecklist = async () => {
  if (!checklistName.value.trim()) {
    error.value = 'Please enter a checklist name';
    return;
  }
  
  loading.value = true;
  error.value = '';
  
  try {
    // Parse items from textarea (split by lines, filter empty)
    const items = itemsText.value
      .split('\n')
      .map(item => item.trim())
      .filter(item => item.length > 0);
    
    const newChecklist = await checklistApi.createChecklist(checklistName.value.trim(), items);
    
    // Reset form and notify parent
    resetForm();
    showForm.value = false;
    emit('checklistCreated', newChecklist.id);
  } catch (err) {
    error.value = 'Failed to create checklist. Please try again.';
    console.error('Error creating checklist:', err);
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  checklistName.value = '';
  itemsText.value = '';
  error.value = '';
};
</script>

<style scoped>
.create-checklist-form {
  margin-bottom: 15px;
}

.create-button {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.create-button:hover {
  background: #218838;
}

.form-container {
  background: white;
  border: 2px solid #28a745;
  border-radius: 6px;
  padding: 15px;
  margin-top: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}

.form-container h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 17px;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 600;
  color: #333;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 2px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #28a745;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 15px;
}

.submit-button {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.submit-button:hover:not(:disabled) {
  background: #218838;
}

.submit-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.cancel-button {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.cancel-button:hover {
  background: #5a6268;
}

.error-message {
  background: #fff5f5;
  color: #cc0000;
  padding: 8px;
  border-radius: 4px;
  margin-top: 10px;
  border: 1px solid #ffcdd2;
  font-size: 14px;
}
</style> 