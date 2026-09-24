import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: "", email: "", phoneNumber: "", password: "", role: "STUDENT", specialization: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(form);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-card" style={{ width: 420 }}>
        <h1>Create account</h1>
        <p className="sub">Students and maintenance workers can self-register</p>
        {error && <div className="error-box">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field-group">
            <label>Full name</label>
            <input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          </div>
          <div className="field-row">
            <div className="field-group">
              <label>College email</label>
              <input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div className="field-group">
              <label>Phone number</label>
              <input value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} />
            </div>
          </div>
          <div className="field-group">
            <label>Password</label>
            <input type="password" required minLength={8} value={form.password}
                   onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <div className="field-group">
            <label>I am a</label>
            <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              <option value="STUDENT">Student</option>
              <option value="WORKER">Maintenance worker</option>
            </select>
          </div>
          {form.role === "WORKER" && (
            <div className="field-group">
              <label>Specialization</label>
              <select value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })}>
                <option value="">Select...</option>
                <option>Electrical</option>
                <option>Plumbing</option>
                <option>Housekeeping</option>
                <option>Internet</option>
                <option>Furniture</option>
                <option>General</option>
              </select>
            </div>
          )}
          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: "100%" }}>
            {loading ? "Creating account..." : "Create account"}
          </button>
        </form>
        <div className="auth-switch">
          Already registered? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
