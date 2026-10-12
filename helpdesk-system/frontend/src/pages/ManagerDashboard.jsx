import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios.js";
import StatusPill from "../components/StatusPill.jsx";

export default function ManagerDashboard() {
  const [stats, setStats] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadData = useCallback(() => {
    setLoading(true);
    setError("");

    Promise.allSettled([
      api.get("/dashboard/stats"),
      api.get("/tickets")
    ]).then(([statsRes, ticketsRes]) => {
      if (statsRes.status === "fulfilled") {
        setStats(statsRes.value.data);
      } else {
        console.error("Failed to load dashboard stats:", statsRes.reason);
      }

      if (ticketsRes.status === "fulfilled") {
        setTickets(ticketsRes.value.data);
      } else {
        const status = ticketsRes.reason?.response?.status;
        const msg = ticketsRes.reason?.response?.data?.message;
        if (status === 403) {
          setError("Access forbidden. Please verify you are signed in with a MANAGER account.");
        } else {
          setError(msg || "Could not load tickets from the server.");
        }
      }
    }).finally(() => {
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const visible = statusFilter === "ALL" ? tickets : tickets.filter((t) => t.status === statusFilter);

  return (
    <>
      <div className="page-heading">
        <div>
          <h1>Manager dashboard</h1>
          <p>All complaints across hostels and campus buildings.</p>
        </div>
        <button className="btn btn-secondary" onClick={loadData} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {error && <div className="error-box">{error}</div>}

      {stats && (
        <div className="stat-row">
          <div className="stat-cell"><div className="stat-num">{stats.open}</div><div className="stat-label">Open</div></div>
          <div className="stat-cell"><div className="stat-num">{stats.assigned}</div><div className="stat-label">Assigned</div></div>
          <div className="stat-cell"><div className="stat-num">{stats.inProgress}</div><div className="stat-label">In progress</div></div>
          <div className="stat-cell"><div className="stat-num">{stats.resolved}</div><div className="stat-label">Resolved</div></div>
          <div className="stat-cell"><div className="stat-num">{stats.closed}</div><div className="stat-label">Closed</div></div>
          <div className="stat-cell"><div className="stat-num">{stats.escalated}</div><div className="stat-label">Escalated</div></div>
        </div>
      )}

      <div className="field-group" style={{ maxWidth: 220 }}>
        <label>Filter by status</label>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="ALL">All ({tickets.length})</option>
          {["OPEN", "ASSIGNED", "ACCEPTED", "IN_PROGRESS", "RESOLVED", "CONFIRMED", "CLOSED", "ESCALATED", "REJECTED", "CANCELLED", "REOPENED"].map((s) => (
            <option key={s} value={s}>{s.replace("_", " ")}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <p>Loading tickets...</p>
      ) : visible.length === 0 ? (
        <div className="empty-state">
          {tickets.length === 0 ? "No complaints have been submitted yet." : "No tickets match this filter."}
        </div>
      ) : (
        <div>
          {visible.map((t) => (
            <Link to={`/tickets/${t.ticketCode}`} className="ticket-row" key={t.id}>
              <span className="ticket-code">#{t.ticketCode}</span>
              <span className="ticket-main">
                <div className="ticket-title">{t.subCategoryName || "General issue"} — {t.studentName}</div>
                <div className="ticket-meta">{t.locationSummary}</div>
              </span>
              <span className="priority-tag">{t.priority}</span>
              <StatusPill status={t.status} />
            </Link>
          ))}
        </div>
      )}
    </>
  );
}
