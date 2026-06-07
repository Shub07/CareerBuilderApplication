# Admin Teachers Module — System Architecture

## 1. Overview

The Admin Teachers module manages **faculty lifecycle** for a multi-tenant school platform. Each school (`school_id`) owns its teachers. The canonical aggregate root is **`Faculty`** (`faculty` table); extended admin UI fields live in **`FacultyProfile`** (1:1, separate table for schema evolution without bloating core entity).

Class assignments use existing **`ClassSubjectTeacher`** (many assignments per teacher).

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│ Admin UI    │────▶│ AdminTeacher     │────▶│ AdminTeacher        │
│ (React)     │     │ Controller       │     │ ManagementService   │
└─────────────┘     └──────────────────┘     └──────────┬──────────┘
                                                        │
                        ┌───────────────────────────────┼───────────────────────────────┐
                        ▼                               ▼                               ▼
                 FacultyRepository            FacultyProfileRepository      ClassSubjectTeacherRepository
                        │                               │                               │
                        ▼                               ▼                               ▼
                   faculty (core)              faculty_profiles                 class_subject_teachers
```

## 2. Scalability principles

| Concern | Approach |
|--------|----------|
| Multi-tenancy | Every query scoped by `school_id` |
| Read scaling | Paginated list; indexed filters; assignment batch-load per page |
| Write scaling | Transactional creates; unique constraints on email/employee code |
| File storage | `AdminFileStorageHelper` → object storage path (swap S3 later) |
| Status / leave | `FacultyProfile.accountStatus`; optional sync from `teacher_leave_requests` |
| Soft delete | `FacultyProfile.deleted` — preserves referential integrity |
| API versioning | Prefix `/api/admin/teachers` — add `/v2` when breaking changes needed |

## 3. File structure

```
src/main/java/com/org/careerbuilder/
├── controller/
│   └── AdminTeacherController.java          # REST API
├── dto/
│   ├── request/AdminTeacherRequests.java    # Request DTOs
│   └── response/AdminTeacherDtos.java       # Response records
├── models/
│   ├── FacultyProfile.java
│   └── enums/
│       ├── FacultyAccountStatus.java
│       └── FacultyEmploymentType.java
├── repository/
│   ├── FacultyRepository.java               # extended queries
│   ├── FacultyProfileRepository.java
│   └── ClassSubjectTeacherRepository.java   # existing + batch helpers
└── service/
    ├── AdminTeacherManagementService.java
    └── impl/
        └── AdminTeacherManagementServiceImpl.java

src/main/resources/db/migration/
└── V1030__admin_teachers_module.sql
```

## 4. Database schema

### `faculty` (existing — core identity)

| Column | Type | Notes |
|--------|------|-------|
| faculty_pk | BIGINT PK | Internal ID |
| faculty_code | VARCHAR(50) UK | Employee ID e.g. T-2024-001 |
| first_name, last_name | VARCHAR(50) | |
| gender | VARCHAR(20) | |
| age | INT | Derived from DOB on save |
| subject_id | BIGINT FK | Primary specialization |
| qualification | VARCHAR(100) | |
| experience_years | INT | |
| phone, email | UK per school scope | |
| address | VARCHAR(300) | |
| school_id | BIGINT FK | Tenant |

### `faculty_profiles` (new — admin portal extension)

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT PK | |
| faculty_id | BIGINT FK UK | 1:1 with faculty |
| account_status | VARCHAR(20) | ACTIVE, ON_LEAVE, INACTIVE |
| photo_url | VARCHAR(500) | |
| date_of_birth | DATE | |
| joining_date | DATE | |
| employment_type | VARCHAR(30) | FULL_TIME, PART_TIME, CONTRACT |
| department | VARCHAR(100) | |
| skills | VARCHAR(500) | Comma-separated |
| certifications | VARCHAR(1000) | |
| resume_path | VARCHAR(500) | |
| id_proof_path | VARCHAR(500) | |
| deleted | BOOLEAN | Soft delete |
| created_at, updated_at | TIMESTAMP | |

### `class_subject_teachers` (existing — assignments)

Unique `(school_id, class_name, section, subject_id)` — drives "Classes" column and **unassigned** count.

## 5. API endpoints

Base: **`/api/admin/teachers`** — all require `schoolId` unless noted.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/stats` | Summary cards: total, active, on leave, unassigned |
| GET | `/filters` | Subjects, statuses, class sections for dropdowns |
| GET | `/academic-years` | Academic year selector (header) |
| GET | `/section` | Paginated table (`q`, `subjectId`, `status`, `statsFilter`, `page`, `size`) |
| POST | `/section/query` | Same filters via JSON body |
| PATCH | `/bulk/status` | Bulk status update (checkbox selection) |
| DELETE | `/bulk` | Bulk soft-delete |
| GET | `/{facultyId}` | Teacher detail for edit/view |
| POST | `/` | Create teacher (multipart: profile + optional files) |
| PUT | `/{facultyId}` | Update teacher |
| PATCH | `/{facultyId}/status` | ACTIVE / ON_LEAVE / INACTIVE |
| DELETE | `/{facultyId}` | Soft delete |
| GET | `/{facultyId}/assignments` | Class-subject assignments |
| POST | `/{facultyId}/assignments` | Assign class + section + subject |
| DELETE | `/{facultyId}/assignments/{assignmentId}` | Deactivate assignment |
| GET | `/export/fields` | Field catalog for export modal (4 categories) |
| POST | `/export/preview` | Count teachers that will be exported |
| POST | `/export` | Generate Excel / CSV / PDF (bulk IDs or current filters) |
| GET | `/{facultyId}/profile` | View Profile (summary header) |
| GET | `/{facultyId}/edit-form` | Edit Teacher modal — pre-filled + dropdowns |
| PUT | `/{facultyId}` | Save teacher (JSON or multipart with documents) |
| GET | `/{facultyId}/delete-preview` | Delete confirmation modal message |
| DELETE / POST | `/{facultyId}` or `/{facultyId}/delete` | Soft-delete teacher + deactivate assignments |
| GET | `/{facultyId}/allocations/form` | Allocate Classes modal — checkboxes + subjects |
| POST | `/{facultyId}/allocations` | Bulk class allocation for one subject |

**Legacy quick-add** (dashboard): `POST /api/admin/teachers` on `AdminOperationsController` delegates to the same service.

---

## 8. Teacher profile page (`/api/admin/teachers/{facultyId}/profile`)

| Method | Path | Tab / feature |
|--------|------|----------------|
| GET | `/` | Full profile header + basic details |
| GET | `/basic-details` | Personal, professional, documents |
| GET | `/allocations` | Class & subject allocation (role + assigned date) |
| GET | `/assignments` | Teacher assignments (class, submission rate, status) |
| GET | `/assignments/{assignmentId}` | **View Assignment** — submission summary + per-student rows |
| GET | `/attendance-leave` | Present/absent/leave balance, recent attendance, leave requests |
| POST | `/leave/{leaveId}/review` | Approve / reject pending leave (action=APPROVE\|REJECT) |
| GET | `/workload` | Weekly classes, subjects/sections assigned, weekly timetable |
| GET | `/employment` | Employment info + salary & contract + employment documents |
| GET | `/employment-documents/{kind}` | Download `resume` or `contract` |
| GET | `/activity-log` | Recent activity (paginated) |
| GET | `/documents` | Document table |
| POST | `/documents` | Upload document (PDF/JPG/PNG, 10MB) |
| GET | `/documents/{id}/download` | Download |
| DELETE | `/documents/{id}` | Delete |
| GET | `/export/fields` | Export profile modal fields |
| POST | `/export` | Export single teacher (Excel/CSV/PDF) |
| GET | `/more-actions` | ⋯ menu options |
| POST | `/mark-leave`, `/deactivate`, `/assign-subject` | Header actions |

**Schema:** `faculty_documents` (V1032), `faculty_profiles.designation`,
allocation `role` + `assigned_date` and `faculty_profiles.basic_salary` /
`contract_type` / `contract_path` (V1033).

## 6. Stat-card filters (`statsFilter`)

Clicking a summary card passes `statsFilter` to `/section`:

| Card | `statsFilter` | Query behavior |
|------|---------------|----------------|
| Total Teachers | `ALL` or omit | No status/unassigned filter |
| Active Teachers | `ACTIVE` | `account_status = ACTIVE` |
| On Leave | `ON_LEAVE` | `account_status = ON_LEAVE` |
| Unassigned | `UNASSIGNED` | No active class assignments |

The **Status** dropdown can refine further (e.g. card Active + dropdown Active).

## 7. Status rules

- **ACTIVE**: Default; teaching normally.
- **ON_LEAVE**: Set manually or when approved leave covers today (future job).
- **INACTIVE**: Not teaching; excluded from active counts.
- **Unassigned**: No row in `class_subject_teachers` with `is_active = true` for this faculty.

## 7. Security (production)

Current app uses permissive JWT. For production:

- `@PreAuthorize("hasRole('SCHOOL_ADMIN')")` on controller
- Validate JWT `schoolId` matches request `schoolId`
- Rate-limit file uploads; virus scan; presigned URLs for downloads
