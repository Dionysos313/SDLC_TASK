import React, { useState } from "react";
import { Task, TaskStatus } from "../types";

type Props = {
  initial?: Task;
  onSave: (task: Task) => Promise<void>;
  onCancel?: () => void;
};

export default function TaskForm({ initial, onSave, onCancel }: Props) {
  const [title, setTitle] = useState(initial?.title ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [dueDate, setDueDate] = useState(initial?.dueDate ?? "");
  const [status, setStatus] = useState<TaskStatus>(initial?.status ?? "TODO");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  function validate(): boolean {
    const e: Record<string, string> = {};
    if (!title.trim()) e.title = "Title is required";
    if (title.length > 100) e.title = "Title max 100 characters";
    if (description.length > 500) e.description = "Description max 500 characters";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function submit(evt?: React.FormEvent) {
    evt?.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      await onSave({
        id: initial?.id,
        title: title.trim(),
        description: description.trim() || undefined,
        dueDate: dueDate || undefined,
        status,
      });
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="card" onSubmit={submit}>
      <div className="form-row">
        <label>Title *</label>
        <input value={title} onChange={e => setTitle(e.target.value)} maxLength={100} />
        {errors.title && <div className="error">{errors.title}</div>}
      </div>

      <div className="form-row">
        <label>Description</label>
        <textarea value={description} onChange={e => setDescription(e.target.value)} maxLength={500} />
        {errors.description && <div className="error">{errors.description}</div>}
      </div>

      <div className="form-row">
        <label>Due Date</label>
        <input type="date" value={dueDate ?? ""} onChange={e => setDueDate(e.target.value)} />
      </div>

      <div className="form-row">
        <label>Status</label>
        <select value={status} onChange={e => setStatus(e.target.value as TaskStatus)}>
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="DONE">DONE</option>
        </select>
      </div>

      <div className="form-actions">
        <button type="submit" disabled={loading}>{initial ? "Update" : "Create"}</button>
        {onCancel && <button type="button" className="secondary" onClick={onCancel}>Cancel</button>}
      </div>
    </form>
  );
}