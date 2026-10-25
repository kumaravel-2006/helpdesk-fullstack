import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios.js";
import StatusPill from "../components/StatusPill.jsx";

export default function StudentDashboard() {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/tickets/mine").then((res) => setTickets(res.data)).finally(() => setLoading(false));
  }, []);

  const counts = tickets.reduce((acc, t) => {
    const bucket = ["OPEN", "REOPENED"].includes(t.status) ? "open"
      : ["ASSIGNED", "ACCEPTED", "IN_PROGRESS"].includes(t.status) ? "inProgress"
      : ["RESOLVED", "CONFIRMED"].includes(t.status) ? "resolved"
      : "closed";
    acc[bucket] = (acc[bucket] || 0) + 1;
    return acc;
  }, {});

  return (
    <>
      <div className="page-heading">
        <div>
          <h1>My complaints</h1>
          <p>Track everything you've raised, from submission to close-out.</p>
        </div>
        <Link to="/new-ticket" className="btn btn-primary">Raise a complaint</Link>
      </div>

      <div className="stat-row">
        <div className="stat-cell"><div className="stat-num">{counts.open || 0}</div><div className="stat-label">Open</div></div>
        <div className="stat-cell"><div className="stat-num">{counts.inProgress || 0}</div><div className="stat-label">In progress</div></div>
        <div className="stat-cell"><div className="stat-num">{counts.resolved || 0}</div><div className="stat-label">Resolved</div></div>
        <div className="stat-cell"><div className="stat-num">{counts.closed || 0}</div><div className="stat-label">Closed</div></div>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : tickets.length === 0 ? (
        <div className="empty-state">No complaints yet. Raise one to get started.</div>
      ) : (
        <div>
          {tickets.map((t) => (
            <Link to={`/tickets/${t.ticketCode}`} className="ticket-row" key={t.id}>
              <span className="ticket-code">#{t.ticketCode}</span>
              <span className="ticket-main">
                <div className="ticket-title">{t.subCategoryName || "General issue"}</div>
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
