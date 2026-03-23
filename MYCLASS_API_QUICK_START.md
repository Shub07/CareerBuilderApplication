# 🚀 MyClass API - Quick Start Guide

## Base URL
```
http://localhost:9091
```

## Endpoints

### 1. Create a MyClass Entry (POST)
**Endpoint**: `POST /api/myclasses`

**Request Body**:
```json
{
  "studentId": 1,
  "subjectId": 2,
  "className": "11th Standard",
  "section": "A"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:9091/api/myclasses \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "subjectId": 2,
    "className": "11th Standard",
    "section": "A"
  }'
```

**PowerShell Example**:
```powershell
$body = @{
    studentId = 1
    subjectId = 2
    className = "11th Standard"
    section = "A"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:9091/api/myclasses" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body
```

---

## Database Verification

To verify the `my_classes` table was created successfully:

```sql
-- Connect to PostgreSQL
psql -h localhost -p 5432 -U admin -d admindb

-- List the table structure
\d my_classes

-- Count existing records
SELECT COUNT(*) FROM my_classes;

-- View all records
SELECT * FROM my_classes;
```

---

## Common Errors & Solutions

### Error: "relation \"my_classes\" does not exist"
**Solution**: Run the database migration script
```powershell
.\run_migration.ps1
```

### Error: "Port 9091 already in use"
**Solution**: Kill the existing Java process
```powershell
Stop-Process -Name java -Force
```

### Error: "Could not connect to database"
**Solution**: Verify PostgreSQL is running and check credentials in `application.properties`

---

## Application Logs Location

When running the application, check logs for:
- Successful table creation
- Entity relationship issues
- SQL execution details

**Log Level**: Currently set to `INFO`

To enable more detailed SQL logging, add to `application.properties`:
```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## Next Steps

1. ✅ Database migration completed
2. ✅ my_classes table created
3. 🔄 Restart the application
4. 🧪 Test the API with sample data
5. 📊 Verify data persistence in the database

---

## Support

For additional information, refer to:
- `DATABASE_MIGRATION_SOLUTION.md` - Detailed migration guide
- `MYCLASS_POST_FIX.md` - MyClass endpoint documentation
- `src/main/java/com/org/careerbuilder/models/MyClass.java` - Entity definition

