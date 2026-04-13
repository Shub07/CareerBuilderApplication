-- Seed data for Leave Request System
-- This script inserts sample leave requests for testing

-- Insert sample leave requests for testing
-- Note: Make sure student IDs exist in your database

INSERT INTO leave_requests (
    student_id, 
    from_date, 
    to_date, 
    leave_type, 
    reason, 
    status, 
    is_half_day, 
    half_day_period,
    approved_on,
    approved_by,
    created_at, 
    updated_at
) VALUES 
-- Student 1 - Approved leave (past)
(1, '2026-01-14', '2026-01-17', 'Family Function', 'Attending family wedding ceremony', 'APPROVED', FALSE, NULL, '2025-11-22 14:30:00', 'Principal', '2025-11-20 10:00:00', '2025-11-22 14:30:00'),

-- Student 1 - Pending leave (upcoming)
(1, '2026-04-10', '2026-04-12', 'Sick Leave', 'Fever and cold', 'APPLIED', FALSE, NULL, NULL, NULL, '2026-03-28 09:15:00', '2026-03-28 09:15:00'),

-- Student 1 - Rejected leave (past)
(1, '2026-02-01', '2026-02-05', 'Medical Emergency', 'Emergency dental procedure', 'REJECTED', FALSE, NULL, NULL, NULL, '2026-01-15 11:30:00', '2026-01-18 10:00:00'),

-- Student 1 - Approved half-day (upcoming)
(1, '2026-04-15', '2026-04-15', 'Medical Checkup', 'Regular medical checkup', 'APPROVED', TRUE, 'morning', '2026-04-01 10:30:00', 'Vice Principal', '2026-04-01 08:00:00', '2026-04-01 10:30:00'),

-- Student 2 - Approved leave (past)
(2, '2026-01-20', '2026-01-22', 'Casual Leave', 'Personal work', 'APPROVED', FALSE, NULL, '2025-11-25 15:00:00', 'Principal', '2025-11-23 14:00:00', '2025-11-25 15:00:00'),

-- Student 2 - Pending leave (upcoming)
(2, '2026-04-20', '2026-04-25', 'Family Function', 'Attending cousin wedding', 'APPLIED', FALSE, NULL, NULL, NULL, '2026-04-01 09:30:00', '2026-04-01 09:30:00'),

-- Student 3 - Approved leave (past)
(3, '2026-03-10', '2026-03-12', 'Sick Leave', 'Cold and cough', 'APPROVED', FALSE, NULL, '2026-03-08 16:00:00', 'Vice Principal', '2026-03-07 08:45:00', '2026-03-08 16:00:00'),

-- Student 3 - Pending half-day (upcoming)
(3, '2026-04-08', '2026-04-08', 'Doctor Appointment', 'Annual checkup', 'APPLIED', TRUE, 'afternoon', NULL, NULL, '2026-03-30 13:20:00', '2026-03-30 13:20:00'),

-- Student 4 - Approved leave (upcoming)
(4, '2026-04-25', '2026-04-28', 'Summer Vacation', 'Early vacation', 'APPROVED', FALSE, NULL, '2026-04-10 09:00:00', 'Principal', '2026-04-05 10:15:00', '2026-04-10 09:00:00'),

-- Student 4 - Cancelled leave (past)
(4, '2026-02-10', '2026-02-12', 'Personal Leave', 'Home decoration', 'CANCELLED', FALSE, NULL, NULL, NULL, '2026-01-25 15:45:00', '2026-02-05 12:00:00');

-- Verify inserted data
SELECT COUNT(*) as total_leave_requests FROM leave_requests;

-- Show upcoming leave requests for student 1
SELECT 
    leave_id,
    student_id,
    from_date,
    to_date,
    leave_type,
    reason,
    status,
    is_half_day,
    half_day_period,
    created_at
FROM leave_requests 
WHERE student_id = 1 
AND from_date >= CURDATE()
ORDER BY from_date ASC;

-- Show all leave requests for student 1 by status
SELECT 
    status,
    COUNT(*) as count
FROM leave_requests 
WHERE student_id = 1
GROUP BY status;
