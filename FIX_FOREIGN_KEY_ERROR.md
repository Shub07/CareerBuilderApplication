# 🔧 FIX - Foreign Key Constraint Error in Students Insert

## ❌ THE PROBLEM

```
ERROR:  Key (school_id)=(2) is not present in table "schools".
insert or update on table "students" violates foreign key constraint "fk_students_school"
```

**Root Cause**: You're trying to insert a student with `school_id=2`, but:
- School with ID=2 doesn't exist in the `schools` table
- Foreign key constraint requires all `school_id` values to exist in `schools` table

---

## ✅ THE SOLUTION

### Step 1: Insert Schools FIRST
Before inserting students, you must insert the schools they reference.

```sql
-- Insert schools (IDs 1 and 2)
INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES 
  (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
  (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015);
```

### Step 2: Insert Subjects (Required for MyClass)
MyClass links students to subjects, so subjects must exist.

```sql
INSERT INTO subjects (subject_id, subject_name)
VALUES 
  (1, 'Mathematics'),
  (2, 'English'),
  (3, 'Physics'),
  (4, 'Chemistry'),
  (5, 'Biology');
```

### Step 3: Insert Students (Now Will Work!)
```sql
INSERT INTO students (id, first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id)
VALUES 
  (1, 'John', 'Doe', 16, '11th Standard', 'A', 1, 'Mr. Doe', '9876543210', 'john.doe@example.com', '123 Main St, Delhi', 1),
  (2, 'Jane', 'Smith', 15, '10th Standard', 'B', 2, 'Ms. Smith', '9876543211', 'jane.smith@example.com', '456 Oak Ave, Mumbai', 2),
  (3, 'Bob', 'Wilson', 17, '12th Standard', 'A', 3, 'Mr. Wilson', '9876543212', 'bob.wilson@example.com', '789 Elm Rd, Delhi', 1);
```

### Step 4: Insert MyClasses (Links Students to Subjects)
```sql
INSERT INTO my_classes (student_id, subject_id, class_name, section)
VALUES 
  (1, 1, 'Mathematics Class', 'A'),
  (1, 2, 'English Class', 'A'),
  (2, 3, 'Physics Class', 'B'),
  (2, 1, 'Mathematics Class', 'B'),
  (3, 4, 'Chemistry Class', 'A');
```

---

## 📊 DATA DEPENDENCY DIAGRAM

```
Schools Table
    ↓
    ├─→ Students Table (foreign key: school_id → schools.id)
    │
    └─→ Subjects Table
            ↓
            └─→ MyClasses Table (foreign keys: student_id → students.id, subject_id → subjects.id)
```

**Insertion Order**:
1. **Schools** (no dependencies)
2. **Subjects** (no dependencies)
3. **Students** (depends on Schools)
4. **MyClasses** (depends on Students and Subjects)

---

## 🔑 FOREIGN KEY CONSTRAINTS

Your schema has these foreign key constraints:

```
students.school_id → schools.id
my_classes.student_id → students.id
my_classes.subject_id → subjects.subject_id
```

**This means**:
- Every `school_id` in students must exist in schools table ✅
- Every `student_id` in my_classes must exist in students table ✅
- Every `subject_id` in my_classes must exist in subjects table ✅

---

## 🧪 EXECUTE COMPLETE DATA INSERT

Run this query in PostgreSQL in this exact order:

```sql
-- 1. SCHOOLS (no dependencies)
INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES 
  (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
  (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015);

-- 2. SUBJECTS (no dependencies)
INSERT INTO subjects (subject_id, subject_name)
VALUES 
  (1, 'Mathematics'),
  (2, 'English'),
  (3, 'Physics'),
  (4, 'Chemistry'),
  (5, 'Biology');

-- 3. STUDENTS (depends on schools)
INSERT INTO students (id, first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id)
VALUES 
  (1, 'John', 'Doe', 16, '11th Standard', 'A', 1, 'Mr. Doe', '9876543210', 'john.doe@example.com', '123 Main St, Delhi', 1),
  (2, 'Jane', 'Smith', 15, '10th Standard', 'B', 2, 'Ms. Smith', '9876543211', 'jane.smith@example.com', '456 Oak Ave, Mumbai', 2),
  (3, 'Bob', 'Wilson', 17, '12th Standard', 'A', 3, 'Mr. Wilson', '9876543212', 'bob.wilson@example.com', '789 Elm Rd, Delhi', 1);

-- 4. MYCLASS (depends on students and subjects)
INSERT INTO my_classes (student_id, subject_id, class_name, section)
VALUES 
  (1, 1, 'Mathematics Class', 'A'),
  (1, 2, 'English Class', 'A'),
  (2, 3, 'Physics Class', 'B'),
  (2, 1, 'Mathematics Class', 'B'),
  (3, 4, 'Chemistry Class', 'A');

-- VERIFY
SELECT * FROM schools;
SELECT * FROM students;
SELECT * FROM subjects;
SELECT * FROM my_classes;
```

---

## ✅ WHAT YOU'LL GET

### Schools Table
| id | school_name | city |
|----|------------|------|
| 1 | Delhi Public School | Delhi |
| 2 | Mumbai Academy | Mumbai |

### Students Table
| id | first_name | last_name | school_id |
|----|-----------|-----------|-----------|
| 1 | John | Doe | 1 |
| 2 | Jane | Smith | 2 |
| 3 | Bob | Wilson | 1 |

### Subjects Table
| subject_id | subject_name |
|-----------|--------------|
| 1 | Mathematics |
| 2 | English |
| 3 | Physics |
| 4 | Chemistry |
| 5 | Biology |

### MyClasses Table
| id | student_id | subject_id | class_name | section |
|----|-----------|-----------|-----------|---------|
| 1 | 1 | 1 | Mathematics Class | A |
| 2 | 1 | 2 | English Class | A |
| 3 | 2 | 3 | Physics Class | B |
| 4 | 2 | 1 | Mathematics Class | B |
| 5 | 3 | 4 | Chemistry Class | A |

---

## 🎯 TESTING YOUR APIs

Now you can test the MyClass API endpoints:

```bash
# Get all classes
curl http://localhost:9091/api/myclasses/all

# Get classes for student 1
curl http://localhost:9091/api/myclasses/student/1

# Get filtered classes
curl "http://localhost:9091/api/myclasses/filter?studentId=1&className=Mathematics&section=A"

# Create new class for student 1, subject 5 (Biology)
curl -X POST http://localhost:9091/api/myclasses \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "subjectId": 5,
    "className": "Biology Class",
    "section": "A"
  }'
```

---

## 📝 KEY TAKEAWAYS

1. **Always insert parent data first** (Schools before Students)
2. **Foreign keys enforce referential integrity** - protects data consistency
3. **Check constraint errors carefully** - they tell you exactly which ID is missing
4. **Insertion order matters** - follow the dependency chain

---

## 🔄 IF YOU GET THE ERROR AGAIN

**Step 1**: Identify the missing ID
```
Key (school_id)=(X) is not present in table "schools"
                    ↑ This is the missing ID
```

**Step 2**: Insert the missing school/subject/etc.
```sql
INSERT INTO schools VALUES (X, ...);
```

**Step 3**: Retry the original insert

---

## ✅ FILE PROVIDED

`INSERT_TEST_DATA.sql` - Complete script with all data in correct order

Just run it in your PostgreSQL client:
1. Open pgAdmin or psql
2. Connect to your `admindb` database
3. Copy and paste the entire SQL script
4. Execute

---

**Status**: ✅ **SOLVED**  
**Root Cause**: Foreign key constraint violation  
**Solution**: Insert schools before students  
**Result**: Data will insert successfully

