export type TaskStatus = "TODO" | "IN_PROGRESS" | "DONE";

export interface Task {
  id?: number;
  title: string;
  description?: string;
  status: TaskStatus;
  dueDate?: string; // ISO date string (YYYY-MM-DD)
  createdAt?: string; // ISO datetime string
  updatedAt?: string; // ISO datetime string
}

export interface TaskFormData {
  title: string;
  description: string;
  status: TaskStatus;
  dueDate: string;
}

export interface TaskFilters {
  status?: TaskStatus;
  searchQuery?: string;
  showOverdue?: boolean;
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
  timestamp: string;
}

export const TASK_STATUS_LABELS: Record<TaskStatus, string> = {
  TODO: "To Do",
  IN_PROGRESS: "In Progress",
  DONE: "Done"
};