import { createContext, useContext, useState } from "react";
import api from "../api/axios.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("helpdesk_user");
    return stored ? JSON.parse(stored) : null;
  });

  async function login(email, password) {
    const { data } = await api.post("/auth/login", { email, password });
    persist(data);
    return data;
  }

  async function register(payload) {
    const { data } = await api.post("/auth/register", payload);
    persist(data);
    return data;
  }

  function persist(data) {
    localStorage.setItem("helpdesk_token", data.token);
    const userInfo = { id: data.userId, fullName: data.fullName, email: data.email, role: data.role };
    localStorage.setItem("helpdesk_user", JSON.stringify(userInfo));
    setUser(userInfo);
  }

  function logout() {
    localStorage.removeItem("helpdesk_token");
    localStorage.removeItem("helpdesk_user");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
