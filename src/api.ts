import { Task, ApiError } from "./types";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080/api/tasks";

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
    console.log("API Client initialized with base URL:", this.baseUrl);
  }

  private async handleResponse<T>(res: Response): Promise<T> {
    if (!res.ok) {
      const contentType = res.headers.get("content-type");
      
      if (contentType?.includes("application/json")) {
        const errorData: ApiError = await res.json();
        throw new ApiException(res.status, errorData);
      }
      
      const text = await res.text();
      throw new ApiException(res.status, {
        status: res.status,
        error: res.statusText,
        message: text || "An unexpected error occurred",
        path: res.url,
        timestamp: new Date().toISOString()
      });
    }

    if (res.status === 204) {
      return null as T;
    }

    return res.json();
  }

  async listTasks(status?: string): Promise<Task[]> {
    const url = status ? `${this.baseUrl}?status=${status}` : this.baseUrl;
    const res = await fetch(url);
    return this.handleResponse<Task[]>(res);
  }

  async getTask(id: number): Promise<Task> {
    const res = await fetch(`${this.baseUrl}/${id}`);
    return this.handleResponse<Task>(res);
  }

  async createTask(task: Partial<Task>): Promise<Task> {
    const res = await fetch(this.baseUrl, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(task),
    });
    return this.handleResponse<Task>(res);
  }

  async updateTask(id: number, task: Partial<Task>): Promise<Task> {
    const res = await fetch(`${this.baseUrl}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(task),
    });
    return this.handleResponse<Task>(res);
  }

  async updateTaskStatus(id: number, status: string): Promise<Task> {
    const res = await fetch(`${this.baseUrl}/${id}/status?status=${status}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
    });
    return this.handleResponse<Task>(res);
  }

  async deleteTask(id: number): Promise<void> {
    const res = await fetch(`${this.baseUrl}/${id}`, { method: "DELETE" });
    return this.handleResponse<void>(res);
  }

  async getOverdueTasks(): Promise<Task[]> {
    const res = await fetch(`${this.baseUrl}/overdue`);
    return this.handleResponse<Task[]>(res);
  }
}

export class ApiException extends Error {
  constructor(
    public readonly status: number,
    public readonly errorData: ApiError
  ) {
    super(errorData.message);
    this.name = "ApiException";
  }

  getValidationErrors(): Record<string, string> | undefined {
    return this.errorData.validationErrors;
  }
}

// Export singleton instance
export const api = new ApiClient(API_BASE);

// Legacy exports for backward compatibility
export const listTasks = (status?: string) => api.listTasks(status);
export const getTask = (id: number) => api.getTask(id);
export const createTask = (task: Partial<Task>) => api.createTask(task);
export const updateTask = (id: number, task: Partial<Task>) => api.updateTask(id, task);
export const deleteTask = (id: number) => api.deleteTask(id);