-- Admin panel: extended student profile, documents, admission enquiries, activity log

CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL UNIQUE,
    admission_number VARCHAR(50) NOT NULL,
    admission_date DATE NOT NULL,
    photo_url VARCHAR(500),
    gender VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    blood_group VARCHAR(10),
    aadhar_number VARCHAR(20),
    religion VARCHAR(50),
    category VARCHAR(30),
    academic_year VARCHAR(20),
    previous_school_details VARCHAR(1000),
    student_type VARCHAR(20) NOT NULL DEFAULT 'NEW_ADMISSION',
    father_name VARCHAR(100),
    father_phone VARCHAR(20),
    mother_name VARCHAR(100),
    mother_phone VARCHAR(20),
    father_occupation VARCHAR(100),
    guardian_name VARCHAR(100),
    guardian_phone VARCHAR(20),
    address_line1 VARCHAR(300),
    address_line2 VARCHAR(300),
    city VARCHAR(100),
    state_name VARCHAR(100),
    pincode VARCHAR(10),
    country VARCHAR(50) DEFAULT 'India',
    transport_required BOOLEAN NOT NULL DEFAULT FALSE,
    hostel_required BOOLEAN NOT NULL DEFAULT FALSE,
    medical_conditions TEXT,
    additional_notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT uk_student_admission_number UNIQUE (admission_number),
    CONSTRAINT fk_student_profile_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_student_profile_admission ON student_profiles(admission_number);

CREATE TABLE IF NOT EXISTS student_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    document_type VARCHAR(40) NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(500) NOT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_student_document_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_student_documents_student ON student_documents(student_id);

CREATE TABLE IF NOT EXISTS admission_enquiries (
    enquiry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    class_applying VARCHAR(50) NOT NULL,
    parent_name VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    notes TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT fk_admission_enquiry_school FOREIGN KEY (school_id) REFERENCES schools(id)
);

CREATE INDEX idx_admission_enquiry_school_status ON admission_enquiries(school_id, status);

CREATE TABLE IF NOT EXISTS admin_activity_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    entity_type VARCHAR(50),
    entity_id BIGINT,
    performed_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_activity_school FOREIGN KEY (school_id) REFERENCES schools(id)
);

CREATE INDEX idx_admin_activity_school_created ON admin_activity_logs(school_id, created_at DESC);

-- Admin exam scheduling (class + date range from Create Exam modal)
ALTER TABLE exams ADD COLUMN IF NOT EXISTS school_id BIGINT NULL;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS class_name VARCHAR(50) NULL;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS end_date DATE NULL;
