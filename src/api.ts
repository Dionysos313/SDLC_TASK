import { Task } from "./types";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080/api/tasks";
console.log("DEBUG: API_BASE =", API_BASE); // debug: will appear in the browser console

async function handleResponse(res: Response) {
  if (!res.ok) {
    const contentType = res.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      const body = await res.json();
      throw { status: res.status, body };
    }
    throw { status: res.status, body: await res.text() };
  }
  return res.status === 204 ? null : res.json();
}

export async function listTasks(): Promise<Task[]> {
  const res = await fetch(API_BASE);
  return handleResponse(res);
}

export async function getTask(id: number): Promise<Task> {
  const res = await fetch(`${API_BASE}/${id}`);
  return handleResponse(res);
}

export async function createTask(task: Task): Promise<Task> {
  const res = await fetch(API_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(task),
  });
  return handleResponse(res);
}

export async function updateTask(id: number, task: Task): Promise<Task> {
  const res = await fetch(`${API_BASE}/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(task),
  });
  return handleResponse(res);
}

export async function deleteTask(id: number): Promise<void> {
  const res = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
  return handleResponse(res);
}