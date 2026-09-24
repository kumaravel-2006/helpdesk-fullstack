import { Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="shell">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">HELPDESK//SSN</span>
          <span className="brand-sub">Campus &amp; Hostel Service Requests</span>
        </div>
        {user && (
          <div className="who">
            <span>{user.fullName}</span>
            <span className="role-tag">{user.role}</span>
            <button className="logout-link" onClick={logout}>Sign out</button>
          </div>
        )}
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
