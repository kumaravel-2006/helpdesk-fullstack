import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios.js";
import StatusPill from "../components/StatusPill.jsx";

export default function WorkerDashboard() {
  const [tickets, setTickets] = useState([]);

  useEffect(() => {
    api.get("/tickets/mine").then((res) => setTickets(res.data));
  }, []);

  return (
    <>
      <div className="page-heading">
        <div>
          <h1>My assignments</h1>
          <p>Tickets assigned to you, in order of most recent.</p>
        </div>
      </div>

      {tickets.length === 0 ? (
        <div className="empty-state">No tickets assigned to you yet.</div>
      ) : (
        <div>
          {tickets.map((t) => (
            <Link to={`/tickets/${t.ticketCode}`} className="ticket-row" key={t.id}>
              <span className="ticket-code">#{t.ticketCode}</span>
              <span className="ticket-main">
                <div className="ticket-title">{t.subCategoryName || "General issue"}</div>
                <div className="ticket-meta">{t.locationSummary} &middot; preferred {t.preferredTimeSlot}</div>
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
