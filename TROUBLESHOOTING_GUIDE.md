# 🔧 Troubleshooting & Utility Commands

## Port Management

### Check if Port 9091 is in Use
```powershell
# Check port usage on Windows
netstat -ano | findstr :9091

# Alternative: PowerShell method
Get-NetTCPConnection -LocalPort 9091 -ErrorAction SilentlyContinue | Select-Object State, OwningProcess
```

### Kill Process Using Port 9091
```powershell
# Method 1: Kill by port
$process = Get-NetTCPConnection -LocalPort 9091 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
if ($process) { Stop-Process -Id $process -Force }

# Method 2: Kill all Java processes
Stop-Process -Name java -Force

# Method 3: Kill specific PID
Stop-Process -Id <PID> -Force
```

---

## PostgreSQL Database Commands

### Connect to Database
```bash
# Connect to career builder database
psql -h localhost -p 5432 -U admin -d admindb

# List all tables
\dt

# Describe my_classes table
\d my_classes

# Show table size
SELECT pg_size_pretty(pg_total_relation_size('my_classes'));

# Exit psql
\q
```

### Verify Table Structure
```sql
-- Check if my_classes table exists
SELECT EXISTS(
    SELECT 1 FROM information_schema.tables 
    WHERE table_name = 'my_classes'
);

-- Get detailed table info
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'my_classes'
ORDER BY ordinal_position;

-- List all constraints
SELECT constraint_name, constraint_type, table_name
FROM information_schema.table_constraints
WHERE table_name = 'my_classes';
```

### Data Operations
```sql
-- Count records in my_classes
SELECT COUNT(*) as total_records FROM my_classes;

-- View all records with details
SELECT 
    mc.id,
    mc.student_id,
    s.first_name || ' ' || s.last_name as student_name,
    mc.subject_id,
    sb.subject_name,
    mc.class_name,
    mc.section
FROM my_classes mc
JOIN students s ON mc.student_id = s.id
JOIN subjects sb ON mc.subject_id = sb.subject_id
ORDER BY mc.id DESC;

-- Delete a specific record
DELETE FROM my_classes WHERE id = 1;

-- Clear all records (use with caution!)
DELETE FROM my_classes;

-- Reset ID sequence
ALTER SEQUENCE my_classes_id_seq RESTART WITH 1;
```

---

## Maven Commands

### Build Project
```bash
# Clean and build
mvn clean package

# Build without running tests
mvn clean package -DskipTests

# Just compile
mvn clean compile
```

### Run Application
```bash
# Run with Maven
mvn spring-boot:run

# Run with custom port (override property)
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9092"
```

### Check Dependencies
```bash
# List dependencies tree
mvn dependency:tree

# Check for outdated dependencies
mvn versions:display-updates

# Check for vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

---

## Application Logs

### Enable Debug Logging
Add to `src/main/resources/application.properties`:
```properties
# SQL logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Spring logging
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.security=DEBUG

# Application logging
logging.level.com.org.careerbuilder=DEBUG

# SQL formatting
spring.jpa.properties.hibernate.format_sql=true
```

### View Application Logs
```powershell
# If running in background, capture output
Start-Transcript -Path ".\app_logs.txt" -Append
mvn spring-boot:run
```

---

## Database Backup & Restore

### Backup Database
```bash
# Backup entire database
pg_dump -h localhost -U admin -d admindb -F c -b -v -f admindb_backup.dump

# Backup as SQL script
pg_dump -h localhost -U admin -d admindb > admindb_backup.sql
```

### Restore Database
```bash
# Restore from dump file
pg_restore -h localhost -U admin -d admindb -v admindb_backup.dump

# Restore from SQL script
psql -h localhost -U admin -d admindb < admindb_backup.sql
```

---

## API Testing with cURL

### Create MyClass
```bash
curl -X POST http://localhost:9091/api/myclasses \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "subjectId": 2,
    "className": "Class A",
    "section": "A"
  }' \
  -i
```

### Get All MyClasses
```bash
curl -X GET http://localhost:9091/api/myclasses -i
```

### Get MyClass by ID
```bash
curl -X GET http://localhost:9091/api/myclasses/1 -i
```

### Update MyClass
```bash
curl -X PUT http://localhost:9091/api/myclasses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "className": "Updated Class",
    "section": "B"
  }' \
  -i
```

### Delete MyClass
```bash
curl -X DELETE http://localhost:9091/api/myclasses/1 -i
```

---

## Database Migration

### Re-run Migration
```powershell
# If you need to re-apply the migration
.\run_migration.ps1

# Or manually with psql
$env:PGPASSWORD = "admin123"
psql -h localhost -p 5432 -U admin -d admindb -f database-migration.sql
$env:PGPASSWORD = ""
```

### Verify Migration
```sql
-- Check foreign key constraints
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'my_classes';

-- Verify data integrity
SELECT COUNT(*) FROM my_classes;
SELECT COUNT(*) FROM students;
SELECT COUNT(*) FROM subjects;
```

---

## Performance Diagnostics

### Check Database Connections
```sql
-- List active connections
SELECT pid, usename, state, query 
FROM pg_stat_activity 
WHERE datname = 'admindb';

-- Kill specific connection if needed
SELECT pg_terminate_backend(pid) 
FROM pg_stat_activity 
WHERE datname = 'admindb' AND pid <> pg_backend_pid();
```

### Monitor Query Performance
```sql
-- Enable query statistics
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- View slowest queries
SELECT query, mean_time, max_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

---

## Common Error Solutions

### "ERROR: relation \"my_classes\" does not exist"
```powershell
# Solution: Run migration
.\run_migration.ps1
```

### "Port 9091 already in use"
```powershell
# Solution: Kill process using port
Stop-Process -Name java -Force
```

### "FATAL: Ident authentication failed for user \"admin\""
```powershell
# Solution: Set password environment variable
$env:PGPASSWORD = "admin123"
psql -h localhost -U admin -d admindb
```

### "Cannot connect to database"
```bash
# Check PostgreSQL is running
Get-Service postgresql-x64-18

# Restart PostgreSQL if needed
Restart-Service postgresql-x64-18
```

---

## Quick Reference

| Task | Command |
|------|---------|
| Check port | `netstat -ano \| findstr :9091` |
| Kill Java | `Stop-Process -Name java -Force` |
| Start app | `mvn spring-boot:run` |
| Connect DB | `psql -h localhost -U admin -d admindb` |
| Run migration | `.\run_migration.ps1` |
| Backup DB | `pg_dump -h localhost -U admin -d admindb > backup.sql` |
| Test API | `curl -X POST http://localhost:9091/api/myclasses ...` |

---

**Last Updated**: March 22, 2026  
**Database Version**: PostgreSQL 18.1  
**Java Version**: OpenJDK 23.0.1  
**Spring Boot Version**: 4.0.0

