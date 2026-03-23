# Career Builder Backend - Startup Guide

## ✅ Build Status
- **Build**: SUCCESSFUL
- **JAR File**: `target/career-builder-0.0.1-SNAPSHOT.jar` (61.99 MB)
- **Compilation Errors**: 0
- **Date**: March 22, 2026

## Prerequisites

### 1. **Java Installation**
Ensure Java 17 or higher is installed:
```powershell
java -version
```

Expected output:
```
openjdk version "17.x.x" or higher
```

### 2. **PostgreSQL Database**
Ensure PostgreSQL is running with the following configuration:

- **Host**: `localhost`
- **Port**: `5432`
- **Username**: `admin`
- **Password**: `admin123`
- **Database**: `admindb`

### 3. **Database Connection Test**
Test the PostgreSQL connection using PowerShell:
```powershell
# Using psql if installed
psql -h localhost -U admin -d admindb -c "SELECT 1;"

# Or using the JDBC connection string
# jdbc:postgresql://localhost:5432/admindb
```

## Running the Application

### Option 1: Using Maven (Development)
```powershell
cd "C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main"
mvn spring-boot:run
```

### Option 2: Using Java JAR (Production)
```powershell
cd "C:\Users\Admin\Downloads\career-builder-backend-public-apis\career-builder-backend-main"
java -jar target/career-builder-0.0.1-SNAPSHOT.jar
```

### Option 3: Using Java JAR with Custom Configuration
```powershell
java -jar target/career-builder-0.0.1-SNAPSHOT.jar `
  --spring.datasource.url=jdbc:postgresql://localhost:5432/admindb `
  --spring.datasource.username=admin `
  --spring.datasource.password=admin123 `
  --server.port=9090
```

## Expected Console Output

When the application starts successfully, you should see:
```
2026-03-22 19:45:00.123  INFO 12345 --- [           main] c.o.c.CareerBuilderApplication           : 🚀 Application started successfully!
2026-03-22 19:45:05.456  INFO 12345 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 9090 (http)
2026-03-22 19:45:05.789  INFO 12345 --- [           main] c.o.c.CareerBuilderApplication           : Started CareerBuilderApplication in 5.634 seconds (process running for 6.234)
```

## API Testing

### 1. **Health Check**
Test if the application is running:
```powershell
curl -X GET http://localhost:9090/test
```

Expected response:
```
Services are up ..
```

### 2. **Sample API Endpoint - Get Students**
```powershell
curl -X GET http://localhost:9090/api/students?page=0&size=10
```

### 3. **Student Classes Overview**
```powershell
curl -X GET "http://localhost:9090/api/student/classes/overview?studentId=1&date=2026-03-22"
```

### 4. **Today's Classes**
```powershell
curl -X GET "http://localhost:9090/api/student/classes/today?studentId=1&date=2026-03-22"
```

## Configuration

The application is configured in `src/main/resources/application.properties`:

```properties
server.port=9090
spring.datasource.url=jdbc:postgresql://localhost:5432/admindb
spring.datasource.username=admin
spring.datasource.password=admin123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### To Customize Configuration

Edit `application.properties` or pass parameters at runtime:

```powershell
java -jar target/career-builder-0.0.1-SNAPSHOT.jar \
  --server.port=8080 \
  --spring.datasource.url=jdbc:postgresql://YOUR_HOST:5432/YOUR_DB
```

## Troubleshooting

### Issue 1: Port Already in Use
**Error**: `Address already in use: bind`

**Solution**: Change the port in application.properties or at runtime:
```powershell
java -jar target/career-builder-0.0.1-SNAPSHOT.jar --server.port=8081
```

### Issue 2: Database Connection Refused
**Error**: `Connection refused to host: localhost:5432`

**Solution**: 
1. Verify PostgreSQL is running:
   ```powershell
   # Windows Services
   Get-Service postgresql-*
   
   # Start if stopped
   Start-Service postgresql-x64-15
   ```

2. Check database credentials in application.properties

### Issue 3: Database Does Not Exist
**Error**: `database "admindb" does not exist`

**Solution**: Create the database using psql:
```powershell
psql -U postgres
CREATE DATABASE admindb;
CREATE USER admin WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE admindb TO admin;
```

### Issue 4: Lombok Not Working (Old Build)
**Error**: `cannot find symbol: variable log`

**Solution**: Rebuild the application:
```powershell
mvn clean compile
mvn clean package
```

## Key Features Available

After successful startup, the following endpoints are available:

### Student Classes API
- `GET /api/student/classes/overview` - Dashboard overview
- `GET /api/student/classes/today` - Today's classes
- `GET /api/student/classes/subjects` - All subjects
- `GET /api/student/classes/calendar` - Calendar view
- `GET /api/student/classes/materials` - Study materials
- `GET /api/student/classes/teachers` - Teacher list
- `GET /api/student/classes/teachers/{id}/profile` - Teacher profile
- `POST /api/student/classes/teachers/{id}/message` - Send message to teacher

### Student Management API
- `GET /api/students` - List all students
- `POST /api/students` - Create new student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student

### Faculty Management API
- `GET /api/faculty` - List all faculty
- `POST /api/faculty` - Create new faculty member
- `PUT /api/faculty/{id}` - Update faculty
- `DELETE /api/faculty/{id}` - Delete faculty

### School Management API
- `GET /api/schools` - List all schools
- `POST /api/schools` - Create new school

### Parent Management API
- `GET /api/parents` - List all parents
- `POST /api/parents` - Create new parent

## Monitoring & Logs

### View Logs in Real-Time
```powershell
# Follow logs during startup
java -jar target/career-builder-0.0.1-SNAPSHOT.jar
```

### Enable Debug Mode
```powershell
java -jar target/career-builder-0.0.1-SNAPSHOT.jar --debug
```

### Custom Log Configuration
Edit or create `logback-spring.xml` in `src/main/resources/` to customize logging levels.

## Performance Notes

- **Java Heap**: Default is auto-configured
- **To increase heap**: `java -Xmx512m -Xms256m -jar career-builder-0.0.1-SNAPSHOT.jar`
- **Database Pool Size**: Configured via HikariCP (default: 10 connections)

## Next Steps

1. ✅ **Build Complete**: JAR file is ready
2. ✅ **Database Ready**: Connect PostgreSQL
3. ✅ **Start Application**: Run using one of the methods above
4. ⬜ **Test APIs**: Use Postman or curl
5. ⬜ **Monitor Logs**: Check console for any errors
6. ⬜ **Frontend Integration**: Connect your frontend application

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review application logs for error messages
3. Verify database connectivity
4. Ensure Java 17+ is installed
5. Check firewall settings for port 9090

---

**Status**: ✅ Application is ready to run  
**Date**: March 22, 2026  
**Build**: career-builder-0.0.1-SNAPSHOT.jar

