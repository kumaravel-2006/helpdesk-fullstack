# Campus & Hostel HelpDesk Management System

A working scaffold for the ticket-based complaint system described in the spec: students raise
tickets, managers triage and assign workers, workers action and resolve them.

## Stack

- **Frontend**: React 18 + Vite, React Router, Axios
- **Backend**: Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA/Hibernate
- **Database**: PostgreSQL
- **Cache**: Redis (category list is cached; ready for OTP/session use)
- **Messaging**: Kafka (ticket status-change events -> notification consumer)
- **Containers**: Docker Compose wires up Postgres, Redis, Kafka/Zookeeper, backend, frontend

## Run it

```bash
docker compose up --build
```

- Frontend: http://localhost:5174
- Backend API: http://localhost:8085/api
- Postgres: localhost:5434 (helpdesk/helpdesk)
- Kafka: localhost:9094

Categories and sub-categories (from the spec's hostel + campus tables) are seeded automatically
on first boot via `DataSeeder`.

To run the backend alone against local Postgres/Redis/Kafka for development:
```bash
cd backend
mvn spring-boot:run
```
And the frontend:
```bash
cd frontend
npm install
npm run dev
```

## What's fully implemented

- **Auth**: register/login, JWT issuance, BCrypt password hashing, role-based route
  protection (`STUDENT` / `WORKER` / `MANAGER` / `ADMIN`) via Spring Security.
- **Categories**: the hostel (Room, Bathroom, Electrical, Plumbing, Internet) and campus
  (Classrooms, Computer Labs, Library) category → sub-category trees from the spec, seeded
  on boot and served through a Redis-cached endpoint.
- **Ticket lifecycle**: `OPEN → ASSIGNED → ACCEPTED → IN_PROGRESS → RESOLVED → CONFIRMED →
  CLOSED`, plus `REJECTED / CANCELLED / REOPENED / ESCALATED`, enforced in `TicketService`
  (e.g. terminal states can't be transitioned except via `REOPENED`).
- **Structured complaint form**: location type → building/hostel → block/floor/room, category
  → sub-category, priority, preferred date/time slot, permission-to-enter flag, image URLs.
- **Role dashboards**: student (their tickets + stats), manager (all tickets, status filter,
  org-wide stats, worker assignment), worker (their assigned tickets).
- **Kafka notifications**: every status change and worker assignment publishes a `TicketEvent`
  to the `ticket-events` topic; a consumer in `NotificationService` picks it up (currently logs
  it — swap in `spring-boot-starter-mail` / an SMS provider to actually deliver it).
- **Ticket notes**: internal manager notes vs. resolution notes visible to the student.

## What's stubbed / intentionally left for you to extend

- **OTP email/phone verification**: `AuthController` has `/verify-email` and `/verify-phone`
  endpoints as placeholders. Wire them to Redis (store OTP with a TTL) + an email/SMS provider.
- **Image upload**: the API accepts `imageUrls` (already-uploaded URLs) rather than handling
  multipart upload to object storage directly — add an `/api/uploads` endpoint backed by S3 /
  MinIO and point the frontend's file input at it.
- **Admin console**: no UI yet for managing buildings/hostels/categories/user accounts; the
  `ADMIN` role exists in the data model and security config but has no dedicated screens.
- **Worker "accept/reject assignment"** and **before/after photo upload on resolution**: the
  status/entity model supports this (`TicketImage.stage`), but the frontend only exposes the
  core status buttons — extend `TicketDetail.jsx`.
- **Search/pagination** on the manager ticket list (currently the last 50 tickets, client-side
  status filter only).
- **Email/phone-verification gating**: registration doesn't yet block login until verified.

## Project layout

```
backend/   Spring Boot app (entity/repository/service/controller/security/config)
frontend/  React app (pages/components/context/api)
docker-compose.yml
```
