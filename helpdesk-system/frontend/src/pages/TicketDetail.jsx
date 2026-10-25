import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../api/axios.js";
import StatusPill from "../components/StatusPill.jsx";
import { useAuth } from "../context/AuthContext.jsx";

const STUDENT_ACTIONS = {
  RESOLVED: ["CONFIRMED", "REOPENED"],
};

const WORKER_ACTIONS = {
  ASSIGNED: ["ACCEPTED"],
  ACCEPTED: ["IN_PROGRESS"],
  IN_PROGRESS: ["RESOLVED"],
};

const MANAGER_ACTIONS = {
  OPEN: ["ESCALATED"],
  CONFIRMED: ["CLOSED"],
  ESCALATED: ["ASSIGNED"],
};

export default function TicketDetail() {
  const { ticketCode } = useParams();
  const { user } = useAuth();
  const [ticket, setTicket] = useState(null);
  const [workers, setWorkers] = useState([]);
  const [selectedWorker, setSelectedWorker] = useState("");
  const [note, setNote] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  function load() {
    api.get(`/tickets/${ticketCode}`).then((res) => setTicket(res.data));
  }

  useEffect(() => { load(); }, [ticketCode]);

  useEffect(() => {
    if (user?.role === "MANAGER" || user?.role === "ADMIN") {
      api.get("/users/workers").then((res) => setWorkers(res.data));
    }
  }, [user]);

  async function changeStatus(status) {
    setBusy(true);
    setError("");
    try {
      await api.patch(`/tickets/${ticketCode}/status`, { status, note });
      setNote("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update the ticket.");
    } finally {
      setBusy(false);
    }
  }

  async function assignWorker() {
    if (!selectedWorker) return;
    setBusy(true);
    setError("");
    try {
      await api.post(`/tickets/${ticketCode}/assign`, { workerId: Number(selectedWorker) });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not assign a worker.");
    } finally {
      setBusy(false);
    }
  }

  if (!ticket) return <p>Loading...</p>;

  const roleActions = user.role === "STUDENT" ? STUDENT_ACTIONS
    : user.role === "WORKER" ? WORKER_ACTIONS
      : MANAGER_ACTIONS;
  const availableActions = roleActions[ticket.status] || [];

  return (
    <>
      <div className="page-heading">
        <div>
          <h1>#{ticket.ticketCode} &middot; {ticket.subCategoryName}</h1>
          <p>{ticket.locationSummary}</p>
        </div>
        <StatusPill status={ticket.status} />
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="detail-grid">
        <div className="detail-panel">
          <h3>Details</h3>
          <div className="kv"><span>Category</span><span>{ticket.categoryName}</span></div>
          <div className="kv"><span>Priority</span><span>{ticket.priority}</span></div>
          <div className="kv"><span>Reported by</span><span>{ticket.studentName}</span></div>
          <div className="kv"><span>Preferred visit</span><span>{ticket.preferredDate || "—"} ({ticket.preferredTimeSlot})</span></div>
          <div className="kv"><span>Assigned worker</span><span>{ticket.assignedWorkerName || "Not yet assigned"}</span></div>
          <div style={{ marginTop: 14 }}>
            <div className="kv" style={{ borderBottom: "none" }}><span>Description</span></div>
            <p style={{ marginTop: 0 }}>{ticket.description}</p>
          </div>
          {ticket.resolutionNotes && (
            <div style={{ marginTop: 14 }}>
              <div className="kv" style={{ borderBottom: "none" }}><span>Resolution notes</span></div>
              <p style={{ marginTop: 0 }}>{ticket.resolutionNotes}</p>
            </div>
          )}
        </div>

        <div className="detail-panel">
          <h3>Actions</h3>

          {(user.role === "MANAGER" || user.role === "ADMIN") && !ticket.assignedWorkerName && (
            <div className="field-group">
              <label>Assign a worker</label>
              <select value={selectedWorker} onChange={(e) => setSelectedWorker(e.target.value)}>
                <option value="">Select worker...</option>
                {workers.map((w) => (
                  <option key={w.id} value={w.id}>{w.fullName} — {w.specialization || "General"}</option>
                ))}
              </select>
              <button className="btn btn-primary" style={{ marginTop: 10 }} disabled={busy || !selectedWorker}
                onClick={assignWorker}>
                Assign
              </button>
            </div>
          )}

          {availableActions.length > 0 ? (
            <div className="field-group">
              <label>Add a note (optional)</label>
              <textarea value={note} onChange={(e) => setNote(e.target.value)}
                placeholder="What changed / resolution details..." />
              <div style={{ display: "flex", gap: 10, marginTop: 10, flexWrap: "wrap" }}>
                {availableActions.map((action) => (
                  <button key={action} className="btn btn-secondary" disabled={busy}
                    onClick={() => changeStatus(action)}>
                    Mark as {action.replace("_", " ").toLowerCase()}
                  </button>
                ))}
              </div>
            </div>
          ) : (
            <p style={{ color: "var(--muted)", fontSize: 14 }}>No actions available for this ticket right now.</p>
          )}
        </div>
      </div>
    </>
  );
}
