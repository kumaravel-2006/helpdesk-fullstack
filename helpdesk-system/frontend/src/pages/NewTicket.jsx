import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios.js";

const TIME_SLOTS = ["ANYTIME", "MORNING", "AFTERNOON", "EVENING"];

export const DEFAULT_HELP_TOPICS = [
  { id: 1, name: "AC-Problem" },
  { id: 2, name: "Acadamic ERP & LMS Related" },
  { id: 3, name: "Attendance System" },
  { id: 4, name: "AV & Projector Problem" },
  { id: 5, name: "Canteen / Cafeteria" },
  { id: 6, name: "Carpentery" },
  { id: 7, name: "CCTV Problem" },
  { id: 8, name: "Civil- Mason" },
  { id: 9, name: "Desktop" },
  { id: 10, name: "Electrical" },
  { id: 11, name: "Email - adding Email Group" },
  { id: 12, name: "Email ID - Password" },
  { id: 13, name: "Fire & Safety" },
  { id: 14, name: "Hostel Gatepass" },
  { id: 15, name: "Hot water" },
  { id: 16, name: "Housekeeping – Hostels" },
  { id: 17, name: "Housekeeping – Other Areas (Except Hostels)" },
  { id: 18, name: "Landscape related works" },
  { id: 19, name: "Laptop" },
  { id: 20, name: "Lift Complaints" },
  { id: 21, name: "Mazdoor - Shifting" },
  { id: 22, name: "Network" },
  { id: 23, name: "Onedrive/Microsoft" },
  { id: 24, name: "Painting" },
  { id: 25, name: "Pest Control" },
  { id: 26, name: "Plumbing" },
  { id: 27, name: "Printer or Scanner" },
  { id: 28, name: "RO -Problem" },
  { id: 29, name: "Scrolling LED Video Wall" },
  { id: 30, name: "Security" },
  { id: 31, name: "Server" },
  { id: 32, name: "Stores, Table, Chair requisition" },
  { id: 33, name: "STP-PROBLEM" },
  { id: 34, name: "Telephone-Intercom problem" },
  { id: 35, name: "Transport" },
  { id: 36, name: "UPS-Problem" },
  { id: 37, name: "Welding" },
  { id: 38, name: "Wifi -LAN Internet" },
];

export default function NewTicket() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({
    locationType: "HOSTEL",
    buildingOrHostelName: "",
    block: "",
    floor: "",
    roomOrArea: "",
    subCategoryId: "",
    priority: "NORMAL",
    description: "",
    preferredDate: "",
    preferredTimeSlot: "ANYTIME",
    permissionToEnterRoom: false,
  });

  useEffect(() => {
    api.get("/categories")
      .then((res) => {
        if (Array.isArray(res.data) && res.data.length > 0) {
          setCategories(res.data);
        }
      })
      .catch((err) => {
        console.warn("Could not load categories from backend, using defaults:", err);
      });
  }, []);

  const topics = categories.length > 0
    ? categories.flatMap((cat) => {
        if (cat.subCategories && cat.subCategories.length > 0) {
          return cat.subCategories.map((sub) => ({
            id: sub.id,
            name: sub.name,
          }));
        }
        return [{ id: cat.id, name: cat.name }];
      })
    : DEFAULT_HELP_TOPICS;

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      const payload = { ...form, subCategoryId: Number(form.subCategoryId) };
      const { data } = await api.post("/tickets", payload);
      navigate(`/tickets/${data.ticketCode}`);
    } catch (err) {
      setError(err.response?.data?.message || "Could not submit the complaint.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <div className="page-heading">
        <div>
          <h1>Raise a complaint</h1>
          <p>Give the exact location so a worker can find it without back-and-forth.</p>
        </div>
      </div>

      {error && <div className="error-box">{error}</div>}

      <form className="panel" onSubmit={handleSubmit}>
        <div className="field-group">
          <label>Where is the problem?</label>
          <select value={form.locationType}
                  onChange={(e) => setForm({ ...form, locationType: e.target.value })}>
            <option value="HOSTEL">Hostel</option>
            <option value="ACADEMIC_BUILDING">Academic / campus building</option>
          </select>
        </div>

        <div className="field-row">
          <div className="field-group">
            <label>{form.locationType === "HOSTEL" ? "Hostel name" : "Building name"}</label>
            <input required value={form.buildingOrHostelName}
                   onChange={(e) => setForm({ ...form, buildingOrHostelName: e.target.value })}
                   placeholder={form.locationType === "HOSTEL" ? "Boys Hostel A" : "IT Block"} />
          </div>
          <div className="field-group">
            <label>Block (optional)</label>
            <input value={form.block} onChange={(e) => setForm({ ...form, block: e.target.value })} />
          </div>
        </div>

        <div className="field-row">
          <div className="field-group">
            <label>Floor</label>
            <input value={form.floor} onChange={(e) => setForm({ ...form, floor: e.target.value })} />
          </div>
          <div className="field-group">
            <label>Room / area</label>
            <input required value={form.roomOrArea}
                   onChange={(e) => setForm({ ...form, roomOrArea: e.target.value })}
                   placeholder={form.locationType === "HOSTEL" ? "B204" : "Lab 302"} />
          </div>
        </div>

        <div className="field-row">
          <div className="field-group">
            <label>Problem type</label>
            <select required value={form.subCategoryId}
                    onChange={(e) => setForm({ ...form, subCategoryId: e.target.value })}>
              <option value="">— Select a Help Topic —</option>
              {topics.map((topic) => (
                <option key={topic.id} value={topic.id}>{topic.name}</option>
              ))}
            </select>
          </div>
          <div className="field-group">
            <label>Priority</label>
            <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
              <option value="LOW">Low</option>
              <option value="NORMAL">Normal</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>
        </div>

        <div className="field-group">
          <label>Describe the issue</label>
          <textarea required value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                    placeholder="Ceiling fan is not rotating since yesterday." />
        </div>

        <div className="field-row">
          <div className="field-group">
            <label>Preferred date</label>
            <input type="date" value={form.preferredDate}
                   onChange={(e) => setForm({ ...form, preferredDate: e.target.value })} />
          </div>
          <div className="field-group">
            <label>Preferred time</label>
            <select value={form.preferredTimeSlot}
                    onChange={(e) => setForm({ ...form, preferredTimeSlot: e.target.value })}>
              {TIME_SLOTS.map((slot) => <option key={slot} value={slot}>{slot}</option>)}
            </select>
          </div>
        </div>

        {form.locationType === "HOSTEL" && (
          <div className="field-group">
            <label style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <input type="checkbox" style={{ width: "auto" }}
                     checked={form.permissionToEnterRoom}
                     onChange={(e) => setForm({ ...form, permissionToEnterRoom: e.target.checked })} />
              Worker has permission to enter the room if I'm not present
            </label>
          </div>
        )}

        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? "Submitting..." : "Submit complaint"}
        </button>
      </form>
    </>
  );
}
