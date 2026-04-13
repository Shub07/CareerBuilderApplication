-- ============================================================================
-- QUICK FIX: Ensure School ID 2 exists for Vacation Creation
-- ============================================================================
-- Run this script to insert missing school data before creating vacations
-- ============================================================================

-- Check existing schools
SELECT 'Current Schools:' as info;
SELECT id, school_name, school_code, city FROM schools ORDER BY id;

-- Insert missing schools if they don't exist
INSERT INTO schools (id, school_name, school_code, address, city, state, country, phone, email, principal_name, established_year)
VALUES
    (1, 'Delhi Public School', 'DPS001', '123 School Lane, Delhi', 'Delhi', 'Delhi', 'India', '9876543200', 'principal@dps.edu.in', 'Dr. Sharma', 2010),
    (2, 'Mumbai Academy', 'MA001', '456 Education Ave, Mumbai', 'Mumbai', 'Maharashtra', 'India', '9876543201', 'principal@mumbaiaca.edu.in', 'Mrs. Patel', 2015)
ON CONFLICT (id) DO NOTHING;

-- Verify schools were inserted
SELECT 'Schools after insert:' as info;
SELECT id, school_name, school_code, city FROM schools ORDER BY id;

-- Now you can retry your vacation POST request with school_id = 1 or 2

