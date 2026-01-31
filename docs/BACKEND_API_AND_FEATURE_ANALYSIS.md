# OralVis Backend & API Analysis — Android Integration Reference

**Purpose:** Single source of truth for building the OralVis Healthcare Clinic native Android app (Jetpack Compose + MVVM + clean architecture).  
**Backend repo:** `oralvis_back` (cloned from https://github.com/oralvistech/oralvis_back.git).  
**Rules:** Backend is **immutable**; Android must use the **same** APIs, business logic, and error contract.

---

## 1. Backend & API Analysis

### 1.1 Base URL & Route Prefixes

| Prefix | Mount in server.js | Description |
|--------|--------------------|-------------|
| `/api` | `userRoutes`, `bookingRoutes`, `slotRoutes`, `iosRoutes` | Auth, booking, slots |
| `/api/admin` | `adminRoutes` | Admin-only |
| `/api/dentist` | `dentistRoutes` | Dentist dashboard / confirm |
| `/api/clinics` | `clinicRoutes` | **Clinic dashboard & operations** |
| `/api/users` | `userProfileRoutes` | User profile |
| `/api/patient` | `patientRoutes` | Patient |
| `/api/blogs` | `blogRoutes` | Blog |
| `/api/reports` | `reportRoutes` | Reports |

**Note:** Default server port is **4000**. Replace base URL in Android (e.g. `https://your-api-host:4000`).

---

### 1.2 Clinic Login & Authentication APIs

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 1 | `/api/login` | POST | No | `{ "phoneNo"?: string, "email"?: string, "password": string }` — at least one of `phoneNo` or `email` required | 200: `{ "message": "Login successful", "user": { "id", "name", "phoneNo", "role", "lastLogin", "image", "clinicId"?(if clinic) } }` — **Cookies:** `accessToken`, `refreshToken` (httpOnly) | 400 Invalid credentials, 429 Too many attempts, 500 |
| 2 | `/api/admin-login` | POST | No | Same as login (relaxed validation) | Same as login | Same |
| 3 | `/api/dev-login` | POST | No | Same as login (dev-only) | Same as login | Same |
| 4 | `/api/refresh-token` | POST | No (uses cookie) | — | 200: `{ "message": "Tokens refreshed successfully" }` — New cookies set | 401 No/invalid/expired refresh token, 500 |
| 5 | `/api/me` | GET | **Yes** (authMiddleware) | — | 200: `req.user` (full user doc) | 401 No token / invalid / expired / blacklisted |
| 6 | `/api/logout` | POST | No | — | 200: `{ "message": "Logout successful" }` — Clears cookies | 500 |

**Clinic registration (OTP):**

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 7 | `/api/clinics/start-registration` | POST | No | `{ "name", "phoneNo", "clinicemail", "clinicpassword", "website"?: string }` | 200: `{ "message": "OTP sent to mobile number" }` | 400 Phone already registered, 503 Redis not ready, 500 |
| 8 | `/api/clinics/verify-registration` | POST | No | `{ "phoneNo", "otp" }` | 201: `{ "message": "Clinic registered successfully", "user": { "id", "name", "phoneNo", "role": "clinic", "image" } }` + cookies | 400 Invalid OTP / expired session, 500 |
| 9 | `/api/clinics/send-otp` | POST | No | `{ "phoneNo" }` | 200: `{ "message": "OTP sent to mobile number" }` | 404 User not found, 500 |

**Auth requirements (Android):**

- **Cookie-based:** Login/verify-registration set `accessToken` (15 min) and `refreshToken` (7 days) as **httpOnly cookies**. For Android, **Needs Confirmation:** whether the backend also returns tokens in the JSON body for mobile; if not, Android must use **Cookie manager** or backend must expose a mobile-friendly token-in-body flow.
- **Protected routes:** Send `Cookie: accessToken=<token>` (or `Authorization: Bearer <token>` — **Needs Confirmation** whether Bearer is supported). `authMiddleware` reads `req.cookies.accessToken` and validates JWT with `ACCESS_TOKEN_SECRET`; also checks Redis blacklist and loads `req.user`.
- **Refresh:** Call `POST /api/refresh-token` with refresh token cookie before access token expiry; new cookies are set in response.

---

### 1.3 Clinic Dashboard & Identity APIs

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 10 | `/api/clinics/clinic-id/:userId` | GET | **Needs Confirmation** | — | 200: `{ "clinicId": ObjectId }` | 400 Invalid user ID, 404 Clinic not found, 500 |
| 11 | `/api/clinics/clinic-profile/:userId` | GET | **Needs Confirmation** | — | 200: clinic doc or `{ "message": "No profile yet", "clinic": null }` | 500 |
| 12 | `/api/clinics/dashboard-stats-clinic/:clinicId` | GET | **Needs Confirmation** | — | 200: `{ "totalPatients", "todaysAppointments", "completedAppointments", "appointmentsOverTime": [ { "_id": "YYYY-MM-DD", "count" } ], "earnings" }` | 400 Invalid clinicId, 500 |
| 13 | `/api/clinics/earning-dashboard-stats/:clinicId` | GET | **Needs Confirmation** | — | 200: `{ "totalPatients", "totalAppointments", "completedAppointments", "earnings" }` | 404 Clinic not found, 500 |
| 14 | `/api/clinics/appointment-status-counts/:clinicId` | GET | **Needs Confirmation** | — | 200: `{ "paid", "pending", "confirmed", "completed", "cancelled" }` (counts) | 400 Invalid clinicId, 500 |
| 15 | `/api/clinics/appointments/:clinicId` | GET | **Needs Confirmation** | — | 200: array of appointments (see 1.5) sorted by date desc, time asc; each has `patientName` (patient/walkin/Unknown) | 500 |
| 16 | `/api/clinics/bookings-by-date/:clinicId?date=YYYY-MM-DD` | GET | **Needs Confirmation** | Query: `date` (required) | 200: `{ "bookings": [...] }` | 400 Date required, 500 |
| 17 | `/api/clinics/calendar/:clinicId` | GET | **Needs Confirmation** | Query: **either** `month`, `year` **or** `date` (YYYY-MM-DD) | **Month:** `{ "DD": { "backend", "walkin" } }`; **Date:** array of `{ "patientName", "slotTime", "appointmentDate", "durationMinutes" }` | 400 Invalid params/clinicId, 404 Clinic not found, 500 |
| 18 | `/api/clinics/calendar/week` | POST | **Needs Confirmation** | Body: `{ "clinicId", "dates": string[] }` | 200: `{ "YYYY-MM-DD": [ { "patientName", "slotTime", "appointmentDate" } ] }` | 400 Invalid clinicId/dates, 404 Clinic not found, 500 |
| 19 | `/api/clinics/clinic-earnings/:clinicId` | GET | **Needs Confirmation** | — | 200: `{ "totalEarnings", "monthlyEarnings": [ { "month", "amount" } ] }` | 404 Clinic not found, 500 |

**Note:** Clinic routes in code **do not** explicitly attach `authMiddleware`; the web portal likely sends cookies. **Needs Confirmation:** which clinic endpoints require authenticated clinic user and whether `clinicId`/`userId` are enforced server-side (e.g. only owner can access their clinic).

---

### 1.4 Appointment Status Management (Clinic-Side)

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 20 | `/api/clinics/cancel-booking/:bookingId` | PATCH | **Needs Confirmation** | — | 200: `{ "message": "Booking cancelled by clinic", "booking" }` | 404 Booking not found, 500 |
| 21 | `/api/clinics/cancel-bookings/date/:clinicId` | PATCH | **Needs Confirmation** | Body: `{ "date": string }` (ISO or YYYY-MM-DD) | 200: `{ "message": "Cancelled N bookings for <date>" }` | 400 Date required, 500 |
| 22 | `/api/clinics/bookings/:bookingId/mark-paid` | PATCH | **Needs Confirmation** | — | 200: `{ "message": "Booking marked as paid", "booking" }` | 400 Only confirmed can be marked paid, 404 Booking not found, 500 |
| 23 | `/api/clinics/bookings/:bookingId/notes` | PATCH | **Needs Confirmation** | Body: `{ "notes": string }` | 200: `{ "message": "Records updated successfully", "booking" }` | 400 Invalid bookingId, 404 Booking not found, 500 |

**Mark as completed:** There is **no** clinic-specific API to set status to `completed`.  
- Dentist: `PATCH /api/dentist/bookings/:bookingId/confirm` sets status to `completed` (no auth on route — **Needs Confirmation**).  
- Admin: `PATCH /api/admin/bookings/:id` with body `{ "status": "completed" }` (admin auth).  
**Needs Confirmation:** How clinic staff mark an appointment “completed” in the web portal (dentist vs admin API or custom flow).

---

### 1.5 Slots, Walk-in, Reschedule (Clinic)

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 24 | `/api/clinics/slotss/:clinicId?date=...` | GET | **Needs Confirmation** | Query: `date`: `"today"` \| `"tomorrow"` \| `"dayafter"` \| `YYYY-MM-DD` | 200: array of slots (with `isAvailable`); for "today" only future times | 400 Invalid clinicId/date, 500 |
| 25 | `/api/clinics/book-walkin` | POST | **Needs Confirmation** | Body: `{ "clinicId", "name", "phoneNo", "email"?, "abhaId"?, "tokenNumber"?, "appointmentDate", "slotTime", "notes"?, "plannedProcedures"?, "doctor"?, "duration"? }` | 201: `{ "message": "Walk-in appointment booked", "booking" }` | 400 Invalid clinicId / slot already booked, 404 Clinic not found, 500 |
| 26 | `/api/reschedule/:bookingId` | POST | **Needs Confirmation** | Body: `{ "newSlotId": ObjectId }` | 200: `{ "message": "Booking rescheduled successfully", "booking" }` | 400 Invalid ID(s) / slot unavailable, 404 Booking not found, 500 |

**Note:** Reschedule uses `booking.slotId` in code; the Booking model schema in repo does not define `slotId`. **Needs Confirmation:** whether `slotId` exists in production schema or is optional.

---

### 1.6 Clinic Profile & Settings

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 27 | `/api/clinics/clinic-profile/:userId` | GET | **Needs Confirmation** | — | 200: clinic (or "No profile yet", clinic null) | 500 |
| 28 | `/api/clinics/clinic-profile/:userId` | POST | **Needs Confirmation** | Body: clinic fields (name, mainarea, introline, address, phoneNo, image, coverimage, noofpatients, yearsofexp, sterlizedequipmentpercentage, coverVideo, services, dentists, mainDoctor, city, fees, about, coordinates) | 201: new clinic | 400 Clinic already exists, 500 |
| 29 | `/api/clinics/clinic-profile/:userId` | PATCH | **Needs Confirmation** | Body: `{ "clinicData", "userData" }` (clinicData includes coordinates) | 200: `{ "message": "Clinic and user updated", "clinic", "user" }` | 404 Clinic or user not found, 500 |

---

### 1.7 Clinical Records & Medical History (Clinic)

| # | Endpoint | Method | Auth | Request | Response | Errors |
|---|----------|--------|------|---------|----------|--------|
| 30 | `/api/clinics/bookings/:bookingId/clinical-records` | POST | **Needs Confirmation** | Body: complaints, observations, diagnoses, notes, prescriptions, vitalSigns, labOrders, files, treatmentPlan, shareWithPatient | 200: `{ "message": "Clinical record saved successfully", "clinicalRecord" }` | 400/404/500 |
| 31 | `/api/clinics/bookings/:bookingId/clinical-records` | GET | **Needs Confirmation** | — | 200: `{ "clinicalRecord" }` | 404 Clinical record not found, 500 |
| 32 | `/api/clinics/bookings/:bookingId/clinical-records/upload` | POST | **Needs Confirmation** | multipart: `file`; body: `type`, `field` | 200: `{ "message", "attachment", "clinicalRecord" }` | 400 No file / invalid bookingId, 404, 500 |
| 33 | `/api/clinics/bookings/:bookingId/clinical-records/upload-prescription-image` | POST | **Needs Confirmation** | multipart: `file`; body: `prescriptionIndex`? | 200: `{ "message", "imageUrl" }` | 400/404/500 |
| 34 | `/api/clinics/bookings/:bookingId/clinical-records/upload-file` | POST | **Needs Confirmation** | multipart: `file` | 200: `{ "message", "file": { "url", "fileName", "fileType", "uploadedAt" } }` | 400/404/500 |
| 35 | `/api/clinics/medical-history` | POST | **Needs Confirmation** | Body: `{ "clinicId", "patientId"?, "walkinPatientId"?, "condition", "details" }` (one of patientId or walkinPatientId) | 200: `{ "message", "medicalHistory" }` | 400/500 |
| 36 | `/api/clinics/medical-history/:clinicId?patientId=...&walkinPatientId=...` | GET | **Needs Confirmation** | Query: one of patientId or walkinPatientId | 200: `{ "medicalHistory": [] }` | 400/500 |

---

### 1.8 Booking Model (Appointment) — Response Shape

Relevant fields for clinic dashboard/list (from Booking model + controller populates):

- `_id`, `patient` (ObjectId ref User), `walkinPatient` (ObjectId ref WalkinPatient), `clinic` (ObjectId ref Clinic)
- `appointmentDate` (Date), `slotTime` (string, e.g. "10:30" or "10:30 AM")
- `status`: `"pending"` \| `"confirmed"` \| `"completed"` \| `"cancelled"` \| `"paid"` \| `"refund-requested"` \| `"refunded"` \| `"cancelled-no-refund"`
- `notes`, `paymentId`, `refundId`, `refundStatus`, `amountPaid`, `durationMinutes`
- Controllers add: `patientName` (patient.name \| walkinPatient.name \| "Unknown"), and populated `patient`, `walkinPatient`, `clinic` where used.

---

## 2. Feature Mapping (Web → Android)

| Feature | Backend support | APIs / notes |
|---------|------------------|--------------|
| **Clinic login** | Yes | POST `/api/login` (email or phoneNo + password); cookies for tokens. |
| **Clinic registration (OTP)** | Yes | Start → verify OTP → user + cookies. |
| **Get current user** | Yes | GET `/api/me` (auth). |
| **Logout** | Yes | POST `/api/logout`; clear cookies. |
| **Refresh token** | Yes | POST `/api/refresh-token` (cookie). |
| **Resolve clinic for user** | Yes | GET `/api/clinics/clinic-id/:userId`. |
| **Clinic profile (view/edit)** | Yes | GET/POST/PATCH `/api/clinics/clinic-profile/:userId`. |
| **Dashboard stats** | Yes | `dashboard-stats-clinic`, `earning-dashboard-stats`, `appointment-status-counts`. |
| **View appointments (all)** | Yes | GET `/api/clinics/appointments/:clinicId`. |
| **View appointments by date** | Yes | GET `bookings-by-date/:clinicId?date=`, GET `calendar/:clinicId?date=`, POST `calendar/week`. |
| **View appointments by month** | Yes | GET `calendar/:clinicId?month=&year=` (returns per-day backend/walkin counts). |
| **Status counts** | Yes | GET `appointment-status-counts/:clinicId` (paid, pending, confirmed, completed, cancelled). |
| **Cancel single booking (clinic)** | Yes | PATCH `cancel-booking/:bookingId`. |
| **Cancel all bookings for a date (clinic)** | Yes | PATCH `cancel-bookings/date/:clinicId` + body `{ "date" }`. |
| **Mark booking as paid** | Yes | PATCH `bookings/:bookingId/mark-paid` (only when status is confirmed). |
| **Mark booking as completed** | **Needs Confirmation** | No clinic endpoint; dentist `confirm` or admin `changeStatus` used. |
| **Update booking notes** | Yes | PATCH `bookings/:bookingId/notes` body `{ "notes" }`. |
| **Date-based filtering** | Yes | Today / selected date / week / month via calendar and bookings-by-date APIs. |
| **Slots for a date** | Yes | GET `slotss/:clinicId?date=today|tomorrow|dayafter|YYYY-MM-DD`. |
| **Book walk-in** | Yes | POST `book-walkin` with patient + date + slot. |
| **Reschedule appointment** | Yes | POST `/api/reschedule/:bookingId` body `{ "newSlotId" }`. |
| **Earnings** | Yes | `clinic-earnings/:clinicId`, `earning-dashboard-stats/:clinicId`. |
| **Clinical records** | Yes | CRUD + upload for clinical records and medical history (see 1.7). |
| **Dentists by clinic** | Yes | GET `/api/clinics/dentists/:clinicId`. |

---

## 3. Status Flow (Text Diagram)

```
                    ┌─────────────┐
                    │   pending   │
                    └──────┬──────┘
                           │ (confirm / pay)
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
    ┌────────────┐  ┌────────────┐  ┌────────────┐
    │ confirmed  │  │    paid     │  │ cancelled  │
    └─────┬──────┘  └──────┬──────┘  └────────────┘
          │                │
          │ mark-paid      │
          │ (clinic)       │
          ▼                │
    ┌────────────┐        │
    │    paid     │◄───────┘
    └─────┬──────┘
          │ (mark completed: dentist confirm or admin changeStatus)
          ▼
    ┌────────────┐
    │  completed │
    └────────────┘

    Clinic cancel: confirmed/paid → cancelled (single or by date).
    Patient cancel (paid): → refund-requested; (confirmed): → cancelled.
```

**Booking status enum (backend):**  
`pending`, `confirmed`, `completed`, `cancelled`, `paid`, `refund-requested`, `refunded`, `cancelled-no-refund`.

---

## 4. Backend Folder Responsibility Breakdown

| Folder / file | Responsibility | Immutable / critical |
|---------------|----------------|----------------------|
| **server.js** | Express app, CORS, cookie parser, body limits, rate limiter, route mounting, Socket.IO, 404 + errorHandler | Do not modify. |
| **config/** | DB, Redis, AWS, Razorpay, email, WhatsApp | Do not modify. |
| **controllers/** | Business logic per domain (clinic, user, booking, admin, dentist, etc.) | Do not modify. |
| **models/** | Mongoose schemas (User, Clinic, Booking, Slot, WalkinPatient, ClinicalRecord, MedicalHistory, etc.) | Do not modify. |
| **routes/** | HTTP method + path → controller; some use validation middleware | Do not modify. |
| **middleware/** | authMiddleware (JWT from cookie, Redis blacklist, req.user), validation (express-validator), errorHandler, rateLimiter, upload | Do not modify. |
| **utils/** | Tokens, S3, PDF, WhatsApp/email helpers, patientId generation | Do not modify. |

**Critical for Android:**  
- **Auth:** Cookie-based access/refresh; Android must align (cookies or confirm token-in-body).  
- **Clinic identity:** `userId` (from `/api/me`) → `clinicId` via `/api/clinics/clinic-id/:userId`.  
- **All clinic features** are under `/api/clinics/*` and booking/reschedule under `/api/*`; no changes to routes or request/response shapes.

---

## 5. Error Handling Contract

### 5.1 Central Error Response (errorHandler.js)

- **Shape:** `{ "success": false, "message": string }`  
- **Optional (non-production):** `"stack": string`  
- **Status code:** From `err.statusCode` / `err.status` or **500**.

### 5.2 Common HTTP Codes Used in Controllers

| Code | Meaning | Typical body |
|------|---------|--------------|
| 200 | OK | Resource or success message + optional data |
| 201 | Created | Message + created resource |
| 400 | Bad request | `{ "error": "..." }` or `{ "message": "..." }` |
| 401 | Unauthorized | `{ "error": "..." }` (e.g. No token, Invalid token, Token expired) |
| 403 | Forbidden | `{ "error": "..." }` (e.g. Dentist/Admin access required) |
| 404 | Not found | `{ "error": "..." }` or `{ "message": "..." }` |
| 409 | Conflict | e.g. slot already held |
| 429 | Too many requests | e.g. login attempts |
| 500 | Server error | `{ "error": "..." }` or `{ "message": "..." }` |
| 503 | Service unavailable | e.g. Redis not ready |

### 5.3 Validation Errors (express-validator)

- **Status:** 400  
- **Body:** `{ "error": "Validation failed", "details": [ { "field", "message", "value" } ] }`

### 5.4 Android Integration Rules

1. **Parse both** `error` and `message` for user-facing text (different controllers use either key).  
2. **Treat 401** as “session expired” → trigger refresh token or re-login.  
3. **Treat 403** as “not allowed” (wrong role).  
4. **Use same success flag:** Backend uses `success: false` in centralized error handler; success responses usually have no `success` field or resource payload.  
5. **Do not assume** a single error key; support both `error` and `message` in error bodies.  
6. **Validation:** On 400 with `details` array, map `field` + `message` to form/UI.

---

## 6. Notes for Android Integration

1. **Base URL:** Configure one base URL (e.g. `https://<host>:4000`); all routes are relative to it (e.g. `/api/login`, `/api/clinics/...`).  
2. **Auth:** Confirm with backend whether Android should use **cookies** (OkHttp CookieJar) or **Bearer token in body/header** for mobile.  
3. **Clinic context:** After login, call `/api/me` then `/api/clinics/clinic-id/:userId` to get `clinicId` for all clinic APIs.  
4. **Date format:** Use **YYYY-MM-DD** for query/body dates; **slotTime** can be "HH:mm" (24h) or "h:mm AM/PM" (backend accepts both in different flows).  
5. **Ids:** All IDs are MongoDB ObjectIds (24-char hex); send as strings.  
6. **Mark completed:** Confirm with product/backend how clinic marks appointment “completed” (dentist vs admin API or new clinic endpoint).  
7. **Clinic route auth:** Confirm which clinic endpoints require auth and that server validates clinic ownership.  
8. **Booking slotId:** Reschedule uses `booking.slotId`; confirm schema in production.  
9. **File uploads:** Clinical record uploads use multipart; Content-Type and file field names must match backend.  
10. **CORS:** Backend allows specific origins; ensure Android app’s API host is allowed if needed for webviews or debugging.

---

## 7. API Quick Reference Table (Clinic + Login)

| Category | Endpoint | Method |
|----------|----------|--------|
| **Auth** | `/api/login` | POST |
| | `/api/refresh-token` | POST |
| | `/api/me` | GET |
| | `/api/logout` | POST |
| | `/api/clinics/start-registration` | POST |
| | `/api/clinics/verify-registration` | POST |
| **Clinic identity** | `/api/clinics/clinic-id/:userId` | GET |
| | `/api/clinics/clinic-profile/:userId` | GET, POST, PATCH |
| **Dashboard** | `/api/clinics/dashboard-stats-clinic/:clinicId` | GET |
| | `/api/clinics/earning-dashboard-stats/:clinicId` | GET |
| | `/api/clinics/appointment-status-counts/:clinicId` | GET |
| **Appointments** | `/api/clinics/appointments/:clinicId` | GET |
| | `/api/clinics/bookings-by-date/:clinicId` | GET |
| | `/api/clinics/calendar/:clinicId` | GET |
| | `/api/clinics/calendar/week` | POST |
| **Status actions** | `/api/clinics/cancel-booking/:bookingId` | PATCH |
| | `/api/clinics/cancel-bookings/date/:clinicId` | PATCH |
| | `/api/clinics/bookings/:bookingId/mark-paid` | PATCH |
| | `/api/clinics/bookings/:bookingId/notes` | PATCH |
| **Slots / walk-in** | `/api/clinics/slotss/:clinicId` | GET |
| | `/api/clinics/book-walkin` | POST |
| | `/api/reschedule/:bookingId` | POST |
| **Earnings** | `/api/clinics/clinic-earnings/:clinicId` | GET |
| **Clinical** | `/api/clinics/bookings/:bookingId/clinical-records` | GET, POST |
| | `/api/clinics/medical-history/:clinicId` | GET |
| | `/api/clinics/medical-history` | POST |

---

*End of document. Use this as the single source of truth for Android development. Do not modify backend; implement app to match APIs and error contract above.*
