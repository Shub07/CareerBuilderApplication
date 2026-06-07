# Admin — Class Details Module

Production-ready backend for the **Class Details** section of the Admin Portal
(`Invitto`). Manages academic classes, their sections, curriculum subjects and
teacher allocations. Built to mirror the conventions already established by the
Admin Students and Admin Teachers modules.

---

## 1. Design goals

- **First-class entities.** Classes/sections were previously only free-text
  strings on `students` / `class_subject_teachers`. This module introduces real
  `academic_classes`, `class_sections` and `class_subjects` tables while keeping
  backward compatibility with the existing string-based linkage (enrollment and
  teacher allocations still resolve by `class_name` / `section`).
- **Multi-tenant.** Every row is scoped by `school_id`; every query is filtered
  by it.
- **Soft delete.** Classes carry a `deleted` flag — list/stat queries ignore
  deleted rows.
- **Scalable reads.** List + activity log are paginated; stats use `COUNT`
  aggregates with supporting indexes.
- **Auditable.** All mutations are recorded via `AdminActivityLogger`
  (`entityType = "CLASS"`), powering the Activity Log tab.

---

## 2. File structure

```
models/
  AcademicClass.java            grade level for an academic year
  ClassSection.java             a division (Section A/B/...) within a class
  ClassSubject.java             curriculum subject + code + assigned teacher
  enums/ClassStatus.java        ACTIVE / INACTIVE
repository/
  AcademicClassRepository.java  search, stats, academic years
  ClassSectionRepository.java
  ClassSubjectRepository.java
dto/request/AdminClassRequests.java
dto/response/AdminClassDtos.java
service/AdminClassService.java
service/impl/AdminClassServiceImpl.java
controller/AdminClassController.java
resources/db/migration/V1034__admin_class_details_module.sql
```

Reuses existing: `Subject`, `Faculty`, `Student`, `ClassSubjectTeacher`
(now with `role` + `assigned_date`), `AdminActivityLog`.

---

## 3. Database schema (`V1034`)

### `academic_classes`
| Column | Type | Notes |
|--------|------|-------|
| id | BIGSERIAL PK | |
| school_id | BIGINT FK schools | |
| name | VARCHAR(50) | e.g. "Grade 10" |
| academic_year | VARCHAR(20) | e.g. "2025-2026" |
| max_capacity | INT | |
| status | VARCHAR(20) | ACTIVE / INACTIVE |
| class_teacher_id | BIGINT FK faculty | NULL ⇒ Unassigned |
| deleted | BOOLEAN | soft delete |
| created_at / updated_at | TIMESTAMP | |

Unique `(school_id, name, academic_year)`.

### `class_sections`
`(id, class_id FK→academic_classes ON DELETE CASCADE, school_id, name,
section_teacher_id FK→faculty, timestamps)` — unique `(class_id, name)`.

### `class_subjects`
`(id, class_id, school_id, subject_id FK→subjects, subject_code,
assigned_teacher_id FK→faculty, timestamps)` — unique `(class_id, subject_id)`.

Enrollment counts derive from `students.class_name` / `section`; teacher
allocations from `class_subject_teachers` (`class_name`, `section`, `role`).

---

## 4. API endpoints (`/api/admin/classes`)

### List page
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/stats` | Total classes / sections / students / unassigned |
| GET | `/academic-years` | Academic-year dropdown |
| GET | `/?q&academicYear&statsFilter&page&size` | Paginated class list |
| POST | `/` | Create Class (name + max capacity) |
| PUT | `/{classId}` | Edit Class (name, year, capacity, status) |
| GET | `/{classId}/delete-preview` | Delete confirmation copy |
| DELETE | `/{classId}` | Soft-delete class |

`statsFilter` ∈ `ALL` · `UNASSIGNED` · `ACTIVE` · `INACTIVE` (stat-card clicks).

### Detail page (`/{classId}`)
| Method | Path | Tab / feature |
|--------|------|----------------|
| GET | `/{classId}` | Header (sections, students, class teacher, status) |
| GET | `/{classId}/form-options` | Teacher / subject / section dropdowns |
| GET | `/{classId}/sections` | Sections tab |
| POST/PUT/DELETE | `/{classId}/sections[/{id}]` | Add / edit / delete section |
| GET | `/{classId}/subjects` | Subjects tab (curriculum) |
| POST/PUT/DELETE | `/{classId}/subjects[/{id}]` | Add / edit / delete subject |
| GET | `/{classId}/teachers` | Teachers tab (allocations, grouped) |
| POST | `/{classId}/teachers` | Assign Teacher (teacher+subject+section+role) |
| DELETE | `/{classId}/teachers/{allocationId}` | Remove allocation group |
| POST | `/{classId}/assign-class-teacher` | Header "Assign Class Teacher" |
| GET | `/{classId}/students?section&page&size` | Students tab (enrolled, paginated) |
| GET | `/{classId}/students/search?q` | Add Student modal — search candidates |
| POST | `/{classId}/students` | Add Student (existing student + section) |
| PATCH | `/{classId}/students/{studentId}/transfer` | Transfer to another section / class |
| DELETE | `/{classId}/students/{studentId}` | Remove student from class roster |
| GET | `/students/bulk-template` | Download Excel template (.xlsx) |
| POST | `/{classId}/students/bulk-preview` (multipart) | Parse + validate upload, return preview rows |
| POST | `/{classId}/students/bulk-upload` (multipart) | Commit bulk enrolment |
| GET | `/{classId}/activity-log?page&size` | Activity Log tab |

---

## 5. Behaviour notes

- **Status lifecycle.** New classes start `INACTIVE` (unassigned). Assigning a
  class teacher flips the class to `ACTIVE` and records a `CLASS_TEACHER`
  allocation when a subject is supplied.
- **Subjects.** `Subject` remains a global, name-unique catalog. Adding a
  subject to a class resolves-or-creates the catalog entry, then stores the
  class-specific `subject_code` and assigned teacher in `class_subjects`.
- **Teacher allocations.** The Teachers tab groups `class_subject_teachers`
  rows by teacher + subject + role and aggregates sections (e.g. "A, B" or
  "ALL"). Removing a row deactivates the whole group. A `null` section ⇒ "ALL".
- **Academic year** defaults to the Indian April–March convention
  (e.g. `2025-2026`).
- **Student enrolment** reuses the existing string-based placement on
  `students` (`class_name` + `section`); enrolled rows are read by class name,
  with `reg number`, `gender` and `status` joined from `student_profiles`
  (batch-loaded by id to avoid N+1). Removing a student does **not** delete the
  record — it un-enrols by setting `class_name = UNASSIGNED`, `section = NA`.
- **Bulk upload** accepts `.xlsx` / `.xls` / `.csv` (Apache POI `WorkbookFactory`
  + manual CSV) with columns *Registration Number, Student Name, Section*.
  Preview validates each row (reg number must resolve to a same-school student;
  section must exist in the class) and marks it `PENDING` or `ERROR`; upload
  commits only valid rows and skips the rest idempotently.
