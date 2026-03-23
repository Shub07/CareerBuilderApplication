# 🚀 STEP-BY-STEP GUIDE TO ADD TEST DATA

## Your Current Situation

You're getting a 404 error because:
- ❌ Student with ID 1 doesn't exist in the database
- ❌ Subject with ID 2 doesn't exist in the database

**Solution**: Add these records to your database

---

## 📍 METHOD 1: Using psql (Command Line) - EASIEST

### Step 1: Open PowerShell/CMD

```powershell
# Open PowerShell or Command Prompt
# Navigate to PostgreSQL bin directory (if needed)
```

### Step 2: Connect to Your Database

```bash
psql -h localhost -U admin -d admindb
```

You should see:
```
admindb=>
```

### Step 3: Insert School Data

```sql
INSERT INTO schools (
  school_id,
  school_name,
  school_code,
  school_type,
  board_affiliation,
  affiliation_number,
  year_of_establishment,
  medium_of_instruction,
  school_category,
  village_town_city,
  district,
  state_ut
) VALUES (
  1,
  'Central High School',
  'CHS-001',
  'Government',
  'CBSE',
  'CBSE-2024',
  '1995',
  'English',
  'Co-Educational',
  'New Delhi',
  'Delhi',
  'Delhi'
);
```

**Expected**: `INSERT 0 1`

### Step 4: Insert Subject Data

```sql
INSERT INTO subjects (subject_id, subject_name) 
VALUES (2, 'Mathematics');
```

**Expected**: `INSERT 0 1`

### Step 5: Insert Student Data

```sql
INSERT INTO students (
  id,
  first_name,
  last_name,
  age,
  class_name,
  section,
  roll_no,
  parent_name,
  phone,
  email,
  address,
  school_id
) VALUES (
  1,
  'John',
  'Doe',
  16,
  '11th Standard',
  'A',
  1,
  'Mr. Doe',
  '9876543210',
  'john.doe@example.com',
  '123 Main Street, New Delhi',
  1
);
```

**Expected**: `INSERT 0 1`

### Step 6: Verify Data

```sql
-- Check Student
SELECT id, first_name, last_name FROM students WHERE id = 1;
-- Expected: 1 | John | Doe

-- Check Subject
SELECT subject_id, subject_name FROM subjects WHERE subject_id = 2;
-- Expected: 2 | Mathematics

-- Check School
SELECT school_id, school_name FROM schools WHERE school_id = 1;
-- Expected: 1 | Central High School
```

### Step 7: Exit psql

```sql
\q
```

---

## 📍 METHOD 2: Using DBeaver (GUI) - VISUAL

### Step 1: Open DBeaver
- Launch DBeaver application

### Step 2: Connect to Database
- Right-click "Databases" → "New Database Connection"
- Select PostgreSQL
- Host: localhost
- Port: 5432
- Username: admin
- Password: admin123
- Database: admindb

### Step 3: Create New SQL Script
- Right-click connection → "SQL Editor"
- Paste the SQL below:

```sql
-- Insert School
INSERT INTO schools (
  school_id, school_name, school_code, school_type,
  board_affiliation, affiliation_number, year_of_establishment,
  medium_of_instruction, school_category, village_town_city,
  district, state_ut
) VALUES (
  1, 'Central High School', 'CHS-001', 'Government',
  'CBSE', 'CBSE-2024', '1995', 'English', 'Co-Educational',
  'New Delhi', 'Delhi', 'Delhi'
);

-- Insert Subject
INSERT INTO subjects (subject_id, subject_name)
VALUES (2, 'Mathematics');

-- Insert Student
INSERT INTO students (
  id, first_name, last_name, age, class_name, section,
  roll_no, parent_name, phone, email, address, school_id
) VALUES (
  1, 'John', 'Doe', 16, '11th Standard', 'A',
  1, 'Mr. Doe', '9876543210', 'john.doe@example.com',
  '123 Main Street, New Delhi', 1
);
```

### Step 4: Execute
- Click the Run button (▶) or press Ctrl+Enter
- Check "Execution log" for success message

### Step 5: Verify
- Expand tables in the left panel
- Right-click "schools" → "View Data"
- Right-click "subjects" → "View Data"  
- Right-click "students" → "View Data"
- All should show your inserted records

---

## 📍 METHOD 3: Using pgAdmin (Web UI) - RECOMMENDED

### Step 1: Open pgAdmin
- Go to http://localhost:5050
- Login with your pgAdmin credentials

### Step 2: Navigate to Database
- Left sidebar: Servers → PostgreSQL → Databases → admindb

### Step 3: Open Query Tool
- Right-click "admindb" → "Query Tool"

### Step 4: Paste SQL

```sql
-- All three inserts at once
INSERT INTO schools (school_id, school_name, school_code, school_type, board_affiliation, affiliation_number, year_of_establishment, medium_of_instruction, school_category, village_town_city, district, state_ut) VALUES (1, 'Central High School', 'CHS-001', 'Government', 'CBSE', 'CBSE-2024', '1995', 'English', 'Co-Educational', 'New Delhi', 'Delhi', 'Delhi');

INSERT INTO subjects (subject_id, subject_name) VALUES (2, 'Mathematics');

INSERT INTO students (id, first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id) VALUES (1, 'John', 'Doe', 16, '11th Standard', 'A', 1, 'Mr. Doe', '9876543210', 'john.doe@example.com', '123 Main Street, New Delhi', 1);
```

### Step 5: Execute
- Click Execute button or F5
- See "Query returned successfully"

### Step 6: View Results
- Click "Data" tab at the bottom
- Verify your inserts

---

## ✅ VERIFICATION QUERIES

After inserting, run these to confirm:

```sql
-- Verify School exists
SELECT * FROM schools WHERE school_id = 1;

-- Verify Subject exists
SELECT * FROM subjects WHERE subject_id = 2;

-- Verify Student exists
SELECT * FROM students WHERE id = 1;
```

Expected results:
- ✅ 1 row from schools table
- ✅ 1 row from subjects table
- ✅ 1 row from students table

---

## 🧪 TEST YOUR API NOW

### In Postman:

```
Method: POST
URL: http://localhost:9090/api/myclasses
Headers: Content-Type: application/json
Body:
{
  "studentId": 1,
  "subjectId": 2,
  "className": "11th Standard",
  "section": "A"
}
```

### Expected Response:
```json
{
  "id": 1,
  "student": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    ...
  },
  "subject": {
    "id": 2,
    "name": "Mathematics"
  },
  "className": "11th Standard",
  "section": "A"
}
```

Status: **200 OK** ✅

---

## 🆘 TROUBLESHOOTING

### Error: "Relation 'schools' does not exist"
**Solution**: Check schema name, might be different
```sql
SELECT table_name FROM information_schema.tables;
```

### Error: "Duplicate key value violates unique constraint"
**Solution**: Records already exist. Use UPDATE instead:
```sql
UPDATE students SET first_name='John', last_name='Doe' WHERE id=1;
```

### Error: "Foreign key constraint"
**Solution**: School must be inserted BEFORE Student. Ensure school_id=1 exists.

### Error: "Column 'school_id' not found"
**Solution**: Check your students table schema:
```sql
\d students  -- in psql
-- or right-click in DBeaver and view structure
```

---

## 🎯 WHAT'S HAPPENING

When you insert:
1. **School** - Required by Student (foreign key)
2. **Subject** - Required by MyClass (foreign key)
3. **Student** - Required by MyClass (foreign key)

Then when you POST:
- Service receives studentId=1, subjectId=2
- Service looks up Student(1) from database ✅ FOUND
- Service looks up Subject(2) from database ✅ FOUND
- Service validates both exist ✅ OK
- Service saves MyClass ✅ SUCCESS
- Returns 200 OK ✅

---

## ✨ You're All Set!

After completing these steps, your API will work perfectly! 🎉

**Next Step**: Test with Postman using the exact request format shown above.

