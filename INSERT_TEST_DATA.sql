-- ==================== SCHOOLS DATA ====================
-- Insert schools first (before inserting students)

INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES
  (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
  (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015);

-- ==================== SUBJECTS DATA ====================
-- Insert subjects (required for MyClass)

INSERT INTO subjects (subject_id, subject_name)
VALUES
  (1, 'Mathematics'),
  (2, 'English'),
  (3, 'Physics'),
  (4, 'Chemistry'),
  (5, 'Biology'),
  (6, 'History'),
  (7, 'Geography');

-- ==================== STUDENTS DATA ====================
-- Now insert students (with existing school_ids)

INSERT INTO students (id, first_name, last_name, age, class_name, section, roll_no, parent_name, phone, email, address, school_id)
VALUES
  (1, 'John', 'Doe', 16, '11th Standard', 'A', 1, 'Mr. Doe', '9876543210', 'john.doe@example.com', '123 Main St, Delhi', 1),
  (2, 'Jane', 'Smith', 15, '10th Standard', 'B', 2, 'Ms. Smith', '9876543211', 'jane.smith@example.com', '456 Oak Ave, Mumbai', 2),
  (3, 'Bob', 'Wilson', 17, '12th Standard', 'A', 3, 'Mr. Wilson', '9876543212', 'bob.wilson@example.com', '789 Elm Rd, Delhi', 1);

-- ==================== MYCLASS DATA ====================
-- Now you can insert MyClasses (links students to subjects)

INSERT INTO my_classes (student_id, subject_id, class_name, section)
VALUES
  (1, 1, 'Mathematics Class', 'A'),
  (1, 2, 'English Class', 'A'),
  (2, 3, 'Physics Class', 'B'),
  (2, 1, 'Mathematics Class', 'B'),
  (3, 4, 'Chemistry Class', 'A');

-- ==================== VERIFICATION ====================
-- Check inserted data

SELECT * FROM schools;
SELECT * FROM students;
SELECT * FROM subjects;
SELECT * FROM my_classes;

