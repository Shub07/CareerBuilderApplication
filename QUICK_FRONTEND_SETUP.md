# 🚀 QUICK REFERENCE - Frontend Integration

## Backend Status: ✅ READY

All backend changes completed and tested. Frontend can now integrate with MyClass API.

---

## API Base URL
```
http://localhost:9091/api/myclasses
```

---

## All API Endpoints

### 1️⃣ CREATE - POST /api/myclasses
```bash
curl -X POST http://localhost:9091/api/myclasses \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "subjectId": 2,
    "className": "Class A",
    "section": "A"
  }'
```
**Response**: 201 Created with MyClass object

---

### 2️⃣ READ ALL - GET /api/myclasses
```bash
curl http://localhost:9091/api/myclasses
```
**Response**: 200 OK with array of MyClass objects

---

### 3️⃣ READ ONE - GET /api/myclasses/{id}
```bash
curl http://localhost:9091/api/myclasses/1
```
**Response**: 200 OK with single MyClass object

---

### 4️⃣ UPDATE - PUT /api/myclasses/{id}
```bash
curl -X PUT http://localhost:9091/api/myclasses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "className": "Updated Class",
    "section": "B"
  }'
```
**Response**: 200 OK with updated MyClass object

---

### 5️⃣ DELETE - DELETE /api/myclasses/{id}
```bash
curl -X DELETE http://localhost:9091/api/myclasses/1
```
**Response**: 204 No Content

---

## Allowed Frontend Origins (CORS)
✅ http://localhost:5173  
✅ http://localhost:3000  
✅ http://localhost:5174  
✅ http://localhost:8080  
✅ http://127.0.0.1:5173  

---

## Allowed HTTP Methods
✅ GET  
✅ POST  
✅ PUT  
✅ DELETE  
✅ PATCH  
✅ OPTIONS  

---

## Frontend Files to Create

### File 1: `src/services/MyClassService.js`
Copy this service file code from FRONTEND_INTEGRATION_GUIDE.md

### File 2: `src/components/MyClass.jsx`
Copy this component code from FRONTEND_INTEGRATION_GUIDE.md

### File 3: Add Route
Add this to your App.jsx or router:
```jsx
<Route path="/myclasses" element={<MyClass />} />
```

---

## JSON Format

### Request Body (POST/PUT)
```json
{
  "studentId": 1,
  "subjectId": 2,
  "className": "Mathematics",
  "section": "A"
}
```

### Response Body (Success)
```json
{
  "id": 1,
  "student": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe"
  },
  "subject": {
    "id": 2,
    "subjectName": "Mathematics"
  },
  "className": "Mathematics",
  "section": "A"
}
```

### Error Response
```json
{
  "timestamp": "2026-03-22T20:57:00",
  "status": 404,
  "message": "MyClass not found with id: 999"
}
```

---

## Backend Changes Summary

| File | Changes |
|------|---------|
| CorsConfig.java | ✅ Enhanced CORS for all frontend ports |
| MyClassController.java | ✅ Added GET by ID, PUT, DELETE endpoints |
| MyClassService.java | ✅ Added getById, update, delete methods |
| MyClassServiceImpl.java | ✅ Implemented all new methods |

---

## Testing

### Test with Postman
1. Create new request
2. Set method to POST
3. URL: http://localhost:9091/api/myclasses
4. Body (JSON):
```json
{
  "studentId": 1,
  "subjectId": 2,
  "className": "Test",
  "section": "A"
}
```
5. Send - Should get 201 Created

---

## Ports
- Backend: `9091`
- Frontend (Vite): `5173`
- Frontend (React): `3000`

---

## Start Commands

```bash
# Backend
cd career-builder-backend-main
mvn clean spring-boot:run

# Frontend
cd your-frontend-project
npm run dev
```

---

## Status
✅ Backend: READY  
✅ API: CONFIGURED  
✅ CORS: ENABLED  
✅ Database: MIGRATED  
🔄 Frontend: AWAITING YOUR CODE

---

**Last Updated**: March 22, 2026  
**Next Step**: Create frontend files as described above

