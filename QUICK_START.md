# QUICK START - Career Builder Backend

## 🚀 Start Here (3 Steps)

### Step 1: Ensure PostgreSQL is Running
```powershell
# Windows - Check if PostgreSQL service is running
Get-Service postgresql-x64-* | Start-Service
```

### Step 2: Navigate to Project Directory
```powershell
cd "C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main"
```

### Step 3: Start the Application
```powershell
# Option A: Using JAR (Fastest)
java -jar target/career-builder-0.0.1-SNAPSHOT.jar

# Option B: Using Maven
mvn spring-boot:run
```

## ✅ Verify It's Running
```powershell
# Should return: "Services are up .."
curl http://localhost:9090/test
```

---

## 🔧 Important Configuration

**Database Connection** (in `application.properties`):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/admindb
spring.datasource.username=admin
spring.datasource.password=admin123
```

**Default Port**: `9090`

---

## 🧪 Sample API Calls

### Get Today's Classes
```powershell
curl "http://localhost:9090/api/student/classes/today?studentId=1&date=2026-03-22"
```

### Get All Students
```powershell
curl "http://localhost:9090/api/students?page=0&size=10"
```

### Get All Faculty
```powershell
curl "http://localhost:9090/api/faculty?page=0&size=10"
```

---

## ❌ Common Issues

| Problem | Solution |
|---------|----------|
| Port 9090 in use | `java -jar target/career-builder-0.0.1-SNAPSHOT.jar --server.port=8080` |
| Can't connect to DB | Verify PostgreSQL is running: `pg_isready -h localhost` |
| JAR not found | Run `mvn clean package` first |
| Compilation errors | Run `mvn clean compile` to verify (should show 0 errors) |

---

## 📚 Complete Documentation

- **Full Setup Guide**: See `STARTUP_GUIDE.md`
- **Technical Details**: See `FIXES_APPLIED.md`
- **Completion Report**: See `COMPLETION_REPORT.md`

---

## 🎯 What Was Fixed

- ❌ 56 Compilation Errors → ✅ 0 Errors
- ❌ Lombok version mismatch → ✅ Fixed (1.18.42)
- ❌ Logger conflicts → ✅ Resolved
- ❌ Invalid Spring starter → ✅ Corrected

---

## 📊 Build Details

- **JAR File**: `target/career-builder-0.0.1-SNAPSHOT.jar` (61.99 MB)
- **Java Version**: 17 or higher required
- **Build Status**: ✅ SUCCESS
- **Last Built**: March 22, 2026

---

## 🔗 Available Endpoints (Sample)

```
GET    /test                                    - Health check
GET    /api/student/classes/overview            - Dashboard
GET    /api/student/classes/today               - Today's classes
GET    /api/student/classes/subjects            - All subjects
GET    /api/student/classes/teachers            - Teacher list
GET    /api/student/classes/materials           - Study materials
GET    /api/students                            - All students
GET    /api/faculty                             - All faculty
GET    /api/schools                             - All schools
GET    /api/parents                             - All parents
```

---

**Status**: ✅ Ready to Deploy | **Date**: March 22, 2026 | **Version**: 0.0.1-SNAPSHOT

