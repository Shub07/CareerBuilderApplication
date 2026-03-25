# 📋 MyClass API - Complete Redesigned Endpoints with Dedicated Paths

## Base URL
```
http://localhost:9091/api/myclasses
```

---

## ✅ ALL ENDPOINTS (8 TOTAL) - Dedicated Paths

### ==================== CREATE ====================

#### 1. Create New MyClass
**Endpoint**: `POST /api/myclasses`  
**Status Code**: `201 Created`  
**Request Body**:
```json
{
  "studentId": 1,
  "subjectId": 2,
  "className": "Mathematics",
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
  "className": "Mathematics",
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
    "className": "Mathematics",
    "section": "A"
  }'
```

**Error Response (404)**:
```json
{
  "status": 404,
  "message": "Student not found with id: 999"
}
```

---

### ==================== READ ====================

#### 2. Get ALL MyClasses (No Filter)
**Endpoint**: `GET /api/myclasses/all`  
**Status Code**: `200 OK`  
**Parameters**: None  
**Purpose**: Fetch all MyClasses from the entire system

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
    "student": { "id": 2, "firstName": "Jane", "lastName": "Doe" },
    "subject": { "id": 3, "subjectName": "English" },
    "className": "English Class",
    "section": "B"
  }
]
```

**cURL Example**:
```bash
curl http://localhost:9091/api/myclasses/all
```

**Frontend Usage**:
```javascript
// Get all classes from all students
const allClasses = await axios.get('http://localhost:9091/api/myclasses/all');
```

---

#### 3. Get MyClasses for SPECIFIC STUDENT
**Endpoint**: `GET /api/myclasses/student/{studentId}`  
**Status Code**: `200 OK`  
**Path Parameter**: `studentId` (Long) - Required  
**Purpose**: Fetch all MyClasses for a specific student

**Example**: `GET /api/myclasses/student/1`

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
    "id": 3,
    "student": { "id": 1, "firstName": "John", "lastName": "Doe" },
    "subject": { "id": 3, "subjectName": "Physics" },
    "className": "Physics Class",
    "section": "A"
  }
]
```

**cURL Example**:
```bash
curl http://localhost:9091/api/myclasses/student/1
```

**Frontend Usage**:
```javascript
// Get classes for student ID 1
const studentClasses = await axios.get('http://localhost:9091/api/myclasses/student/1');
```

---

#### 4. Get Filtered MyClasses (With Filters)
**Endpoint**: `GET /api/myclasses/filter`  
**Status Code**: `200 OK`  
**Query Parameters**: All required
| Param | Type | Required | Description |
|-------|------|----------|-------------|
| studentId | Long | ✅ YES | Student ID |
| className | String | ✅ YES | Class name to filter |
| section | String | ✅ YES | Section to filter |

**Purpose**: Fetch MyClasses with specific filters (className and section)

**Example**: `GET /api/myclasses/filter?studentId=1&className=Math&section=A`

**Response**:
```json
[
  {
    "id": 1,
    "student": { "id": 1, "firstName": "John", "lastName": "Doe" },
    "subject": { "id": 2, "subjectName": "Mathematics" },
    "className": "Math",
    "section": "A"
  }
]
```

**cURL Example**:
```bash
curl "http://localhost:9091/api/myclasses/filter?studentId=1&className=Math&section=A"
```

**Frontend Usage**:
```javascript
// Get filtered classes
const filteredClasses = await axios.get('http://localhost:9091/api/myclasses/filter', {
  params: {
    studentId: 1,
    className: 'Math',
    section: 'A'
  }
});
```

---

#### 5. Get MyClass by ID
**Endpoint**: `GET /api/myclasses/{id}`  
**Status Code**: `200 OK`  
**Path Parameter**: `id` (Long) - MyClass ID  
**Purpose**: Fetch a specific MyClass by its ID

**Example**: `GET /api/myclasses/1`

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

**cURL Example**:
```bash
curl http://localhost:9091/api/myclasses/1
```

**Error Response (404)**:
```json
{
  "status": 404,
  "message": "MyClass not found with id: 999"
}
```

**Frontend Usage**:
```javascript
// Get specific class by ID
const myClass = await axios.get('http://localhost:9091/api/myclasses/1');
```

---

### ==================== UPDATE ====================

#### 6. Update MyClass
**Endpoint**: `PUT /api/myclasses/{id}`  
**Status Code**: `200 OK`  
**Path Parameter**: `id` (Long) - MyClass ID  
**Request Body** (Both fields optional - partial update):
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
  "student": { "id": 1, "firstName": "John", "lastName": "Doe" },
  "subject": { "id": 2, "subjectName": "Mathematics" },
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

**Frontend Usage**:
```javascript
// Update class
const updated = await axios.put('http://localhost:9091/api/myclasses/1', {
  className: 'Advanced Mathematics',
  section: 'A+'
});
```

---

### ==================== DELETE ====================

#### 7. Delete MyClass by ID
**Endpoint**: `DELETE /api/myclasses/{id}`  
**Status Code**: `204 No Content`  
**Path Parameter**: `id` (Long) - MyClass ID  
**Purpose**: Delete a specific MyClass

**Example**: `DELETE /api/myclasses/1`

**Response**: Empty (No Content)

**cURL Example**:
```bash
curl -X DELETE http://localhost:9091/api/myclasses/1
```

**Frontend Usage**:
```javascript
// Delete specific class
await axios.delete('http://localhost:9091/api/myclasses/1');
```

---

#### 8. Delete ALL MyClasses for Student
**Endpoint**: `DELETE /api/myclasses/student/{studentId}`  
**Status Code**: `204 No Content`  
**Path Parameter**: `studentId` (Long) - Student ID  
**Purpose**: Delete all MyClasses for a specific student (Admin action)  
**⚠️ WARNING**: This deletes all classes for the student!

**Example**: `DELETE /api/myclasses/student/1`

**Response**: Empty (No Content)

**cURL Example**:
```bash
curl -X DELETE http://localhost:9091/api/myclasses/student/1
```

**Frontend Usage**:
```javascript
// Delete all classes for student
await axios.delete('http://localhost:9091/api/myclasses/student/1');
```

---

## 📊 Endpoint Summary Table

| # | Method | Endpoint | Purpose | Status | Frontend Usage |
|---|--------|----------|---------|--------|---|
| 1 | POST | `/api/myclasses` | Create new class | 201 | `POST /myclasses` |
| 2 | GET | `/api/myclasses/all` | Get all classes | 200 | Get all data |
| 3 | GET | `/api/myclasses/student/{id}` | Get for student | 200 | **Get student's classes** |
| 4 | GET | `/api/myclasses/filter` | Get filtered | 200 | **Get filtered data** |
| 5 | GET | `/api/myclasses/{id}` | Get by ID | 200 | Get details |
| 6 | PUT | `/api/myclasses/{id}` | Update | 200 | Update class |
| 7 | DELETE | `/api/myclasses/{id}` | Delete | 204 | Delete class |
| 8 | DELETE | `/api/myclasses/student/{id}` | Delete student's all | 204 | Delete student classes |

---

## 🎯 Frontend Integration Guide

### Using Axios Service

**Create the service file** (`src/services/MyClassService.js`):
```javascript
import axios from 'axios';

const API_BASE = 'http://localhost:9091/api/myclasses';

export const MyClassAPI = {
  // CREATE
  create: (data) => axios.post(API_BASE, data),

  // READ - All different endpoints
  getAll: () => axios.get(`${API_BASE}/all`),
  getByStudent: (studentId) => axios.get(`${API_BASE}/student/${studentId}`),
  getFiltered: (studentId, className, section) =>
    axios.get(`${API_BASE}/filter`, {
      params: { studentId, className, section }
    }),
  getById: (id) => axios.get(`${API_BASE}/${id}`),

  // UPDATE
  update: (id, data) => axios.put(`${API_BASE}/${id}`, data),

  // DELETE
  delete: (id) => axios.delete(`${API_BASE}/${id}`),
  deleteByStudent: (studentId) => axios.delete(`${API_BASE}/student/${studentId}`),
};
```

### Using in React Component

**Example Component Usage**:
```jsx
import { MyClassAPI } from '../services/MyClassService';

function MyClassesPage({ studentId }) {
  const [classes, setClasses] = useState([]);

  // Load student's classes
  const loadMyClasses = async () => {
    try {
      const response = await MyClassAPI.getByStudent(studentId);
      setClasses(response.data);
    } catch (error) {
      console.error('Failed to load classes:', error);
    }
  };

  // Create new class
  const handleCreateClass = async (formData) => {
    try {
      await MyClassAPI.create({
        studentId,
        subjectId: formData.subjectId,
        className: formData.className,
        section: formData.section,
      });
      loadMyClasses(); // Refresh list
    } catch (error) {
      console.error('Failed to create class:', error);
    }
  };

  // Delete a class
  const handleDeleteClass = async (classId) => {
    if (window.confirm('Are you sure?')) {
      try {
        await MyClassAPI.delete(classId);
        loadMyClasses();
      } catch (error) {
        console.error('Failed to delete class:', error);
      }
    }
  };

  useEffect(() => {
    loadMyClasses();
  }, [studentId]);

  return (
    <div>
      {/* Your UI components */}
    </div>
  );
}
```

---

## 🔒 CORS Configuration

✅ Allowed Origins:
- http://localhost:5173 (Vite)
- http://localhost:3000 (React)
- http://localhost:5174 (Alternative)

✅ Allowed Methods:
- GET, POST, PUT, DELETE, PATCH, OPTIONS

✅ Allowed Headers:
- Content-Type, Authorization, X-Requested-With, Accept, Origin

---

## 📌 Key Features

✅ **Separate dedicated paths** - Clear endpoint for each operation  
✅ **Easy frontend mapping** - Each endpoint has one purpose  
✅ **Flexible filtering** - `/filter` endpoint for advanced queries  
✅ **Student-specific** - `/student/{id}` for student's classes  
✅ **Comprehensive logging** - All operations logged  
✅ **Error handling** - Clear error messages  
✅ **RESTful design** - Follows REST principles  

---

## 🧪 Complete Testing Examples

### Test 1: Create Class
```bash
curl -X POST http://localhost:9091/api/myclasses \
  -H "Content-Type: application/json" \
  -d '{"studentId":1,"subjectId":2,"className":"Math","section":"A"}'
# Expected: 201 Created
```

### Test 2: Get All Classes
```bash
curl http://localhost:9091/api/myclasses/all
# Expected: 200 OK with array of all classes
```

### Test 3: Get Classes for Student 1
```bash
curl http://localhost:9091/api/myclasses/student/1
# Expected: 200 OK with classes for student 1
```

### Test 4: Get Filtered Classes
```bash
curl "http://localhost:9091/api/myclasses/filter?studentId=1&className=Math&section=A"
# Expected: 200 OK with filtered classes
```

### Test 5: Get Specific Class
```bash
curl http://localhost:9091/api/myclasses/1
# Expected: 200 OK with class details
```

### Test 6: Update Class
```bash
curl -X PUT http://localhost:9091/api/myclasses/1 \
  -H "Content-Type: application/json" \
  -d '{"className":"Advanced Math","section":"A+"}'
# Expected: 200 OK with updated class
```

### Test 7: Delete Class
```bash
curl -X DELETE http://localhost:9091/api/myclasses/1
# Expected: 204 No Content
```

### Test 8: Delete Student's All Classes
```bash
curl -X DELETE http://localhost:9091/api/myclasses/student/1
# Expected: 204 No Content
```

---

## 📊 Status Codes Reference

| Code | Meaning |
|------|---------|
| 200 | OK - Successful GET |
| 201 | Created - Successful POST |
| 204 | No Content - Successful DELETE/PUT |
| 400 | Bad Request - Invalid data |
| 404 | Not Found - Resource not found |
| 500 | Server Error - Internal issue |

---

## ✅ Ready for Frontend

Your MyClass API is now fully redesigned with:
- ✅ 8 dedicated endpoints
- ✅ Clear separation of concerns
- ✅ Easy frontend integration
- ✅ Comprehensive documentation
- ✅ Error handling
- ✅ Logging

**Status**: 🟢 **PRODUCTION READY**

---

*Last Updated: March 24, 2026*  
*All endpoints designed for frontend integration*

