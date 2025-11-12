import React, { useEffect, useMemo, useState } from "react";
import { Task } from "../types";
import * as api from "../api";
import TaskForm from "./TaskForm";

type GroupBy = "STATUS" | "DUEDATE";
type SortDir = "ASC" | "DESC";

const STATUS_ORDER: Record<string, number> = {
  TODO: 0,
  IN_PROGRESS: 1,
  DONE: 2,
};

function startOfDay(ts: number) {
  const d = new Date(ts);
  d.setHours(0, 0, 0, 0);
  return d.getTime();
}

function dueCategory(task: Task) {
  if (!task.dueDate) return "No due date";
  const today = startOfDay(Date.now());
  const dt = startOfDay(new Date(task.dueDate).getTime());
  if (dt < today) return "Overdue";
  if (dt === today) return "Today";
  return "Upcoming";
}

export default function TaskList() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Task | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [showCreate, setShowCreate] = useState(false);          

  // grouping + sort + search
  const [groupBy, setGroupBy] = useState<GroupBy>("STATUS");
  const [sortDir, setSortDir] = useState<SortDir>("ASC");
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});
  const [query, setQuery] = useState<string>("");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const data = await api.listTasks();
      setTasks(data);
    } catch (e: any) {
      setError(JSON.stringify(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(task: Task) {
    try {
      await api.createTask(task);
      setShowCreate(false);
      await load();
    } catch (e: any) {
      setError(JSON.stringify(e));
    }
  }

  async function handleUpdate(task: Task) {
    try {
      if (task.id == null) throw new Error("Missing id");
      await api.updateTask(task.id, task);
      setEditing(null);
      await load();
    } catch (e: any) {
      setError(JSON.stringify(e));
    }
  }

  async function handleDelete(id?: number) {
    if (!id) return;
    if (!confirm("Delete task?")) return;
    try {
      await api.deleteTask(id);
      await load();
    } catch (e: any) {
      setError(JSON.stringify(e));
    }
  }

  async function changeStatus(task: Task, newStatus: Task["status"]) {
    try {
      if (!task.id) return;
      await api.updateTask(task.id, { ...task, status: newStatus });
      await load();
    } catch (e: any) {
      setError(JSON.stringify(e));
    }
  }

  function toggleSortDir() {
    setSortDir((d) => (d === "ASC" ? "DESC" : "ASC"));
  }

  function toggleGroupExpanded(key: string) {
    setExpanded((s) => ({ ...s, [key]: !s[key] }));
  }

  const grouped = useMemo(() => {
    // start with a copy and apply title filter
    const all = tasks.slice();
    const q = query.trim().toLowerCase();
    const filtered = q ? all.filter((t) => (t.title ?? "").toLowerCase().includes(q)) : all;

    // sort comparator used inside groups
    const cmp = (a: Task, b: Task) => {
      // primary: dueDate (earlier first), fallback: title
      const ta = a.dueDate ? new Date(a.dueDate).getTime() : Number.POSITIVE_INFINITY;
      const tb = b.dueDate ? new Date(b.dueDate).getTime() : Number.POSITIVE_INFINITY;
      if (ta !== tb) return ta < tb ? -1 : 1;
      return a.title.localeCompare(b.title);
    };

    if (sortDir === "DESC") {
      filtered.sort((a, b) => -cmp(a, b));
    } else {
      filtered.sort(cmp);
    }

    if (groupBy === "STATUS") {
      const map: Record<string, Task[]> = { TODO: [], IN_PROGRESS: [], DONE: [] };
      for (const t of filtered) {
        const key = t.status ?? "TODO";
        if (!map[key]) map[key] = [];
        map[key].push(t);
      }
      return map;
    } else {
      // group by due category
      const map: Record<string, Task[]> = {};
      for (const t of filtered) {
        const key = dueCategory(t);
        if (!map[key]) map[key] = [];
        map[key].push(t);
      }
      // ensure stable ordering of groups for UI
      const ordered: Record<string, Task[]> = {};
      const order = ["Overdue", "Today", "Upcoming", "No due date"];
      for (const k of order) {
        if (map[k]) ordered[k] = map[k];
      }
      for (const k of Object.keys(map)) {
        if (!ordered[k]) ordered[k] = map[k];
      }
      return ordered;
    }
  }, [tasks, groupBy, sortDir, query]);

  return (
    <div>
      <div className="header">
        <h1>Task Manager</h1>
        <div>
          <button onClick={() => setShowCreate((s) => !s)}>{showCreate ? "Close" : "New Task"}</button>
          <button onClick={load} className="secondary">
            Refresh
          </button>
        </div>
      </div>

      <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12 }}>
        <label style={{ fontWeight: 600 }}>Group:</label>
        <select value={groupBy} onChange={(e) => setGroupBy(e.target.value as GroupBy)}>
          <option value="STATUS">By status</option>
          <option value="DUEDATE">By due date</option>
        </select>

        <label style={{ fontWeight: 600, marginLeft: 12 }}>Sort:</label>
        <button onClick={toggleSortDir} className="secondary" style={{ marginLeft: 8 }}>
          {sortDir === "ASC" ? "Asc" : "Desc"}
        </button>

        {/* Search input placed at far right */}
        <div style={{ marginLeft: "auto", display: "flex", gap: 8, alignItems: "center" }}>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Filter by title"
            aria-label="Filter tasks by title"
            style={{ padding: 8, borderRadius: 4, border: "1px solid #ddd", minWidth: 200 }}
          />
          <button onClick={() => setQuery("")} className="secondary">
            Clear
          </button>
        </div>
      </div>

      {error && <div className="error">Error: {error}</div>}

      {showCreate && <TaskForm onSave={handleCreate} onCancel={() => setShowCreate(false)} />}

      {editing && <TaskForm initial={editing} onSave={handleUpdate} onCancel={() => setEditing(null)} />}

      {loading ? (
        <div>Loading...</div>
      ) : (
        Object.keys(grouped).map((group) => {
          const list = (grouped as Record<string, Task[]>)[group];
          const isEmpty = !list || list.length === 0;
          const isExpanded = expanded[group] ?? true;
          return (
            <div key={group} className="card" style={{ marginBottom: 10 }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div style={{ fontWeight: 700 }}>
                  {group} ({list ? list.length : 0})
                </div>
                <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                  <button onClick={() => toggleGroupExpanded(group)} className="secondary">
                    {isExpanded ? "Collapse" : "Expand"}
                  </button>
                </div>
              </div>

              {isExpanded && !isEmpty && (
                <table className="tasks" style={{ marginTop: 10 }}>
                  <thead>
                    <tr>
                      <th>Title</th>
                      <th>Status</th>
                      <th>Due</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {list.map((t) => (
                      <tr key={t.id}>
                        <td>{t.title}</td>
                        <td>
                          <select value={t.status} onChange={(e) => changeStatus(t, e.target.value as Task["status"])}>
                            <option value="TODO">TODO</option>
                            <option value="IN_PROGRESS">IN_PROGRESS</option>
                            <option value="DONE">DONE</option>
                          </select>
                        </td>
                        <td>{t.dueDate ?? "-"}</td>
                        <td>
                          <button onClick={() => setEditing(t)}>Edit</button>
                          <button onClick={() => handleDelete(t.id)} className="danger">
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              {isExpanded && isEmpty && <div style={{ marginTop: 8 }}>No tasks in this group.</div>}
            </div>
          );
        })
      )}
    </div>
  );
}