#!/usr/bin/env powershell

# ============================================================
# PostgreSQL Attendance Migration Script
# ============================================================

Write-Host "================================================"
Write-Host "Attendance Management System - PostgreSQL Migration"
Write-Host "================================================"
Write-Host ""

# PostgreSQL connection details
$PG_HOST = "localhost"
$PG_PORT = "5432"
$PG_USER = "admin"
$PG_PASSWORD = "admin123"
$PG_DB = "admindb"
$MIGRATION_FILE = "attendance_migration_postgresql.sql"

# Set environment variable for password (to avoid prompt)
$env:PGPASSWORD = $PG_PASSWORD

# Check if migration file exists
if (-not (Test-Path $MIGRATION_FILE)) {
    Write-Host "ERROR: Migration file '$MIGRATION_FILE' not found!" -ForegroundColor Red
    Write-Host "Please ensure the file is in the current directory."
    exit 1
}

Write-Host "Connecting to PostgreSQL Database..." -ForegroundColor Yellow
Write-Host "Host: $PG_HOST"
Write-Host "Port: $PG_PORT"
Write-Host "Database: $PG_DB"
Write-Host "User: $PG_USER"
Write-Host ""

# Execute migration
Write-Host "Executing migration script: $MIGRATION_FILE" -ForegroundColor Yellow
Write-Host ""

try {
    # Use psql to execute the migration file
    # Note: psql must be installed and in your PATH
    $result = & psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d $PG_DB -f $MIGRATION_FILE -v ON_ERROR_STOP=1

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "================================================"
        Write-Host "Migration completed successfully!" -ForegroundColor Green
        Write-Host "================================================"
        Write-Host ""
        Write-Host "Created/Updated Tables:"
        Write-Host "  OK - attendance_records (enhanced)"
        Write-Host "  OK - attendance_settings"
        Write-Host "  OK - attendance_batch_uploads"
        Write-Host "  OK - attendance_exceptions"
        Write-Host "  OK - attendance_notifications"
        Write-Host "  OK - attendance_audit_log"
        Write-Host "  OK - attendance_report_cache"
        Write-Host ""
        Write-Host "The application will now be able to use these tables."
        Write-Host "================================================"
    } else {
        Write-Host ""
        Write-Host "================================================"
        Write-Host "Migration failed!" -ForegroundColor Red
        Write-Host "================================================"
        Write-Host "Error details above."
        exit 1
    }
}
catch {
    Write-Host ""
    Write-Host "================================================"
    Write-Host "Error executing migration!" -ForegroundColor Red
    Write-Host "================================================"
    Write-Host "Error: $_"
    Write-Host ""
    Write-Host "Make sure PostgreSQL is installed and psql is in your PATH."
    Write-Host "You can also manually execute the migration with:"
    Write-Host "  psql -h $PG_HOST -p $PG_PORT -U $PG_USER -d $PG_DB -f $MIGRATION_FILE"
    exit 1
}
finally {
    # Clear the password from environment
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

exit 0

