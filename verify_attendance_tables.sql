-- ============================================================
-- ATTENDANCE TABLES VERIFICATION SCRIPT
-- ============================================================

-- List all attendance-related tables
SELECT
    table_name,
    'Table' as object_type
FROM information_schema.tables
WHERE table_schema = 'public'
AND table_name LIKE 'attendance_%'
ORDER BY table_name;

-- Count rows in each table
SELECT
    'attendance_settings' as table_name,
    COUNT(*) as row_count
FROM attendance_settings
UNION ALL
SELECT 'attendance_batch_uploads', COUNT(*) FROM attendance_batch_uploads
UNION ALL
SELECT 'attendance_exceptions', COUNT(*) FROM attendance_exceptions
UNION ALL
SELECT 'attendance_notifications', COUNT(*) FROM attendance_notifications
UNION ALL
SELECT 'attendance_audit_log', COUNT(*) FROM attendance_audit_log
UNION ALL
SELECT 'attendance_report_cache', COUNT(*) FROM attendance_report_cache
UNION ALL
SELECT 'attendance_records', COUNT(*) FROM attendance_records
ORDER BY table_name;

-- Show columns in each table
SELECT
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name LIKE 'attendance_%'
ORDER BY table_name, ordinal_position;

