import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios.js";
import StatusPill from "../components/StatusPill.jsx";

export default function WorkerDashboard() {
  const [tickets, setTickets] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = useCallback(() => {
    // 1. Fetch assigned tickets
    api.get("/tickets/mine")
      .then((res) => setTickets(res.data))
      .catch((err) => console.error("Could not fetch tickets:", err));

    // 2. Fetch asynchronous task assignment alerts delivered via Kafka into Redis
    api.get("/notifications/mine")
      .then((res) => {
        if (Array.isArray(res.data)) {
          setNotifications(res.data);
        }
      })
      .catch((err) => console.error("Could not fetch notifications:", err))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadData();
    // Poll every 10 seconds to catch real-time Kafka-dispatched assignments
    const interval = setInterval(loadData, 10000);
    return () => clearInterval(interval);
  }, [loadData]);

  async function handleDismiss(eventId) {
    try {
      await api.delete(`/notifications/${eventId}`);
      setNotifications((prev) => prev.filter((n) => n.eventId !== eventId));
    } catch (err) {
      console.error("Failed to dismiss notification:", err);
    }
  }

  async function handleClearAll() {
    try {
      await api.delete("/notifications");
      setNotifications([]);
    } catch (err) {
      console.error("Failed to clear notifications:", err);
    }
  }

  return (
    <>
      <div className="page-heading">
        <div>
          <h1>Technician Dashboard</h1>
          <p>Tickets assigned to you, synchronized via Kafka event stream.</p>
        </div>
        <button className="btn btn-secondary" onClick={loadData}>
          Refresh
        </button>
      </div>

      {/* Asynchronous Kafka Task Assignment Message Box */}
      <div className="worker-inbox-section">
        <div className="worker-inbox-card">
          <div className="worker-inbox-header">
            <div className="worker-inbox-title">
              <span>🔔 Assignment Inbox</span>
              <span className="kafka-badge">
                <span className="kafka-dot"></span>
                Kafka Event Stream
              </span>
            </div>
            {notifications.length > 0 && (
              <button
                className="btn-dismiss"
                onClick={handleClearAll}
                title="Clear all messages"
              >
                Clear all ({notifications.length})
              </button>
            )}
          </div>

          <div className="worker-inbox-list">
            {notifications.length === 0 ? (
              <div style={{ padding: "16px 20px", color: "var(--muted)", fontSize: 13, background: "var(--panel)" }}>
                ✓ No pending alerts. New task assignments will arrive here in real time via Kafka.
              </div>
            ) : (
              notifications.map((item, idx) => (
                <div key={item.eventId || idx} className="worker-alert-item">
                  <div className="worker-alert-top">
                    <div className="worker-alert-headline">
                      <span className="worker-alert-code">#{item.ticketCode}</span>
                      {item.priority && (
                        <span className="worker-alert-priority">{item.priority}</span>
                      )}
                      <strong style={{ fontSize: 14 }}>{item.title || "New Task Assigned"}</strong>
                    </div>
                    <span className="worker-alert-time">
                      {item.timestamp
                        ? new Date(item.timestamp).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
                        : "Just now"}
                    </span>
                  </div>

                  <p className="worker-alert-body">{item.message}</p>

                  {item.location && (
                    <div className="worker-alert-loc">
                      📍 <strong>Location:</strong> {item.location}
                    </div>
                  )}

                  <div className="worker-alert-actions">
                    <Link to={`/tickets/${item.ticketCode}`} className="btn-open-task">
                      View Ticket &amp; Start Work &rarr;
                    </Link>
                    {item.eventId && (
                      <button
                        className="btn-dismiss"
                        onClick={() => handleDismiss(item.eventId)}
                      >
                        Dismiss
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Assigned Tickets List */}
      <h3 style={{ fontSize: 16, marginBottom: 14, color: "var(--ink)" }}>All My Assigned Tickets</h3>
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
