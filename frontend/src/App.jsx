import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import StudentDashboard from "./pages/StudentDashboard.jsx";
import NewTicket from "./pages/NewTicket.jsx";
import TicketDetail from "./pages/TicketDetail.jsx";
import ManagerDashboard from "./pages/ManagerDashboard.jsx";
import WorkerDashboard from "./pages/WorkerDashboard.jsx";
import { useAuth } from "./context/AuthContext.jsx";

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === "MANAGER" || user.role === "ADMIN") return <ManagerDashboard />;
  if (user.role === "WORKER") return <WorkerDashboard />;
  return <StudentDashboard />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<Layout />}>
        <Route path="/" element={<ProtectedRoute><HomeRedirect /></ProtectedRoute>} />
        <Route path="/new-ticket" element={
          <ProtectedRoute roles={["STUDENT"]}><NewTicket /></ProtectedRoute>
        } />
        <Route path="/tickets/:ticketCode" element={
          <ProtectedRoute><TicketDetail /></ProtectedRoute>
        } />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
