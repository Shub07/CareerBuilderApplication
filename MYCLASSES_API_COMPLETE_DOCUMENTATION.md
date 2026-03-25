# 📋 MyClass API - Complete Endpoint Documentation

## Base URL
```
http://localhost:9091/api/myclasses
```

---

## ✅ All Endpoints (8 Total)

### ==================== CREATE ENDPOINTS ====================

#### 1. Create New MyClass
**Endpoint**: `POST /api/myclasses`  
**Status Code**: `201 Created`

**Request Body**:
```json
{
  "studentId": 1,
  "subjectId": 2,
  "className": "Mathematics Class",
  "section": "A"
}
```

**Response**:
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
  "className": "Mathematics Class",
  "section": "A"
}
```

**Error Response (400)**:
```json
{
  "status": 400,
  "message": "Student is required"
}
```

**Error Response (404)**:
```json
{
  "status": 404,
  "message": "Student not found with id: 999"
}
```

---

### ==================== READ ENDPOINTS ====================

#### 2. Get ALL MyClasses (No Filter)
**Endpoint**: `GET /api/myclasses/all`  
**Status Code**: `200 OK`  
**Parameters**: None

**Response**:
```json
[
  {
    "id": 1,
    "student": { "id": 1, "firstName": "John", "lastName": "Doe" },
    "subject": { "id": 2, "subjectName": "Mathematics" },
    "className": "Math Class",
    "section": "A"
  },
  {
    "id": 2,
    "student": { "id": 1, "firstName": "John", "lastName": "Doe" },
    "subject": { "id": 3, "subjectName": "English" },
    "className": "English Class",
    "section": "A"
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:9091/api/myclasses/all
```

---

#### 3. Get MyClasses (Flexible - Optional studentId)
**Endpoint**: `GET /api/myclasses`  
**Status Code**: `200 OK`

**Parameters**:
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| studentId | Long | Optional | Student ID to filter |
| className | String | Optional | Class name to filter |
| section | String | Optional | Section to filter |

**Example 1: Get All**
```bash
curl -X GET http://localhost:9091/api/myclasses
```

**Example 2: Get for Specific Student**
```bash
curl -X GET "http://localhost:9091/api/myclasses?studentId=1"
```

**Example 3: Get with All Filters**
```bash
curl -X GET "http://localhost:9091/api/myclasses?studentId=1&className=Math&section=A"
```

**Response**:
```json
[
  {
    "id": 1,
    "student": { "id": 1, "firstName": "John", "lastName": "Doe" },
    "subject": { "id": 2, "subjectName": "Mathematics" },
    "className": "Math Class",
    "section": "A"
  }
]
```

---

#### 4. Get MyClass by ID
**Endpoint**: `GET /api/myclasses/{id}`  
**Status Code**: `200 OK`

**Path Parameters**:
| Param | Type | Description |
|-------|------|-------------|
| id | Long | MyClass ID |

**Example**:
```bash
curl -X GET http://localhost:9091/api/myclasses/1
```

**Response**:
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
  "className": "Mathematics Class",
  "section": "A"
}
```

**Error Response (404)**:
```json
{
  "status": 404,
  "message": "MyClass not found with id: 999"
}
```

---

### ==================== UPDATE ENDPOINTS ====================

#### 5. Update MyClass
**Endpoint**: `PUT /api/myclasses/{id}`  
**Status Code**: `200 OK`

**Path Parameters**:
| Param | Type | Description |
|-------|------|-------------|
| id | Long | MyClass ID |

**Request Body** (Partial Update - Only send fields to update):
```json
{
  "className": "Advanced Mathematics",
  "section": "A+"
}
```

**Response**:
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
  "className": "Advanced Mathematics",
  "section": "A+"
}
```

**cURL Example**:
```bash
curl -X PUT http://localhost:9091/api/myclasses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "className": "Advanced Mathematics",
    "section": "A+"
  }'
```

**Error Response (404)**:
```json
{
  "status": 404,
  "message": "MyClass not found with id: 999"
}
```

---

### ==================== DELETE ENDPOINTS ====================

#### 6. Delete MyClass by ID
**Endpoint**: `DELETE /api/myclasses/{id}`  
**Status Code**: `204 No Content`

**Path Parameters**:
| Param | Type | Description |
|-------|------|-------------|
| id | Long | MyClass ID |

**Example**:
```bash
curl -X DELETE http://localhost:9091/api/myclasses/1
```

**Response**: Empty (No Content)

**Error Response (404)**:
```json
{
  "status": 404,
  "message": "MyClass not found with id: 999"
}
```

---

#### 7. Delete ALL MyClasses (Admin Only)
**Endpoint**: `DELETE /api/myclasses`  
**Status Code**: `204 No Content`  
**WARNING**: ⚠️ This deletes ALL MyClasses from database!

**Example**:
```bash
curl -X DELETE http://localhost:9091/api/myclasses
```

**Response**: Empty (No Content)

**⚠️ Use with caution - This is irreversible!**

---

## 📊 Summary Table

| # | Method | Endpoint | Purpose | Status |
|---|--------|----------|---------|--------|
| 1 | POST | `/api/myclasses` | Create new MyClass | 201 |
| 2 | GET | `/api/myclasses/all` | Get all MyClasses | 200 |
| 3 | GET | `/api/myclasses` | Get with optional filters | 200 |
| 4 | GET | `/api/myclasses/{id}` | Get by ID | 200 |
| 5 | PUT | `/api/myclasses/{id}` | Update MyClass | 200 |
| 6 | DELETE | `/api/myclasses/{id}` | Delete by ID | 204 |
| 7 | DELETE | `/api/myclasses` | Delete ALL | 204 |

---

## 🔍 Request & Response Formats

### MyClass Object Structure
```json
{
  "id": 1,
  "student": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "age": 16,
    "className": "10th Grade",
    "section": "A",
    "rollNo": "001",
    "email": "john@example.com",
    "phone": "9876543210",
    "address": "123 Main St",
    "parentName": "Jane Doe"
  },
  "subject": {
    "id": 2,
    "subjectName": "Mathematics",
    "subjectId": 2
  },
  "className": "Mathematics Class",
  "section": "A"
}
```

### Common Error Responses

**400 Bad Request**:
```json
{
  "timestamp": "2026-03-24T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data"
}
```

**404 Not Found**:
```json
{
  "timestamp": "2026-03-24T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "MyClass not found with id: 999"
}
```

**500 Internal Server Error**:
```json
{
  "timestamp": "2026-03-24T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## 🛠️ Testing with cURL

### Create
```bash
curl -X POST http://localhost:9091/api/myclasses \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "subjectId": 2,
    "className": "Math",
    "section": "A"
  }'
```

### Read All
```bash
curl http://localhost:9091/api/myclasses/all
```

### Read Filtered
```bash
curl "http://localhost:9091/api/myclasses?studentId=1"
```

### Read by ID
```bash
curl http://localhost:9091/api/myclasses/1
```

### Update
```bash
curl -X PUT http://localhost:9091/api/myclasses/1 \
  -H "Content-Type: application/json" \
  -d '{"className": "Advanced Math", "section": "A+"}'
```

### Delete
```bash
curl -X DELETE http://localhost:9091/api/myclasses/1
```

### Delete All
```bash
curl -X DELETE http://localhost:9091/api/myclasses
```

---

## 📱 Frontend Integration

### JavaScript/React Example
```javascript
// Axios
const API_BASE = 'http://localhost:9091/api/myclasses';

// Get All
const getAll = () => axios.get(`${API_BASE}/all`);

// Get Filtered
const getFiltered = (studentId) => 
  axios.get(API_BASE, { params: { studentId } });

// Get by ID
const getById = (id) => axios.get(`${API_BASE}/${id}`);

// Create
const create = (data) => axios.post(API_BASE, data);

// Update
const update = (id, data) => axios.put(`${API_BASE}/${id}`, data);

// Delete
const delete = (id) => axios.delete(`${API_BASE}/${id}`);

// Delete All
const deleteAll = () => axios.delete(API_BASE);
```

---

## 🔐 CORS Configuration

✅ Allowed Origins:
- http://localhost:5173 (Vite)
- http://localhost:3000 (React)
- http://localhost:5174 (Alternative)

✅ Allowed Methods:
- GET, POST, PUT, DELETE, PATCH, OPTIONS

✅ Allowed Headers:
- Content-Type, Authorization, X-Requested-With, Accept, Origin

---

## 📈 Performance Notes

- ✅ Lazy loading on relationships (FetchType.LAZY)
- ✅ Pagination ready (can add in future)
- ✅ Indexed queries for student and subject
- ✅ Unique constraint prevents duplicates

---

## 🔒 Security Notes

- ✅ Input validation on all endpoints
- ✅ Resource not found checks
- ✅ Error messages don't expose sensitive data
- ✅ CORS properly configured
- ✅ No authentication required (add if needed)

---

## 📝 Status Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Request successful |
| 201 | Created - Resource created |
| 204 | No Content - Success, no response body |
| 400 | Bad Request - Invalid input |
| 404 | Not Found - Resource doesn't exist |
| 500 | Server Error - Internal issue |

---

**Last Updated**: March 24, 2026  
**API Version**: 1.0  
**Status**: ✅ Production Ready

