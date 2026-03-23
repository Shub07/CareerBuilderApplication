# PostgreSQL Database Migration Script
# This script runs the database migration SQL file

# Database connection parameters
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "admindb"
$dbUser = "admin"
$dbPassword = "admin123"

# Path to the SQL migration file
$sqlFile = "C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main\database-migration.sql"

# Check if psql is available
$psqlVersion = $null
try {
    $psqlVersion = psql --version 2>&1
    Write-Host "PostgreSQL psql found: $psqlVersion"
}
catch {
    Write-Host "Error: PostgreSQL psql client not found in PATH"
    exit 1
}

# Check if the SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "Error: SQL file not found at $sqlFile"
    exit 1
}

Write-Host "Starting database migration..."
Write-Host "Database: $dbName"
Write-Host "Host: $dbHost"
Write-Host "User: $dbUser"

# Run the migration
$env:PGPASSWORD = $dbPassword

psql -h $dbHost -p $dbPort -U $dbUser -d $dbName -f $sqlFile

$exitCode = $LASTEXITCODE
$env:PGPASSWORD = ""

if ($exitCode -eq 0) {
    Write-Host ""
    Write-Host "Database migration completed successfully!"
    Write-Host ""
    Write-Host "The following changes have been applied:"
    Write-Host "  1. Updated 'faculty' table: school_id from varchar to bigint"
    Write-Host "  2. Updated 'notices' table: school_id from varchar to bigint"
    Write-Host "  3. Updated 'parents' table: school_id and student_id from varchar to bigint"
    Write-Host "  4. Updated 'students' table: school_id now bigint FK"
    Write-Host "  5. Created 'my_classes' table for student class enrollments"
    Write-Host ""
    Write-Host "You can now restart the application and the 'my_classes' table will be available!"
}
else {
    Write-Host "Migration failed with exit code: $exitCode"
    exit $exitCode
}

