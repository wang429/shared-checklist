export interface ChecklistItem {
  id: number;
  content: string;
  progress: Record<string, boolean>; // username -> checked status
}

export interface ChecklistSummary {
  id: number;
  name: string;
}

export interface User {
  id: string;
  username: string;
}

export interface ChecklistResponse {
  items: ChecklistItem[];
}

export interface ToggleItemRequest {
  checklistId: number;
  itemId: number;
  userId: string;
} 