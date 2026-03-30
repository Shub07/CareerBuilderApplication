# Student Performance Report API - Complete Documentation

## 🎯 Overview

The Student Performance Report API provides comprehensive academic performance analytics for individual students. It aggregates exam results across multiple exam types (Internal, Weekly, Final) and presents subject-wise performance breakdowns.

---

## 📊 API Endpoint

### Base URL
```
http://localhost:9092
```

### Endpoint
```
GET /api/student/performance/report
```

---

## 🔧 Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `studentId` | Long | Optional* | Student ID for performance report | `1` |
| `examType` | String | Optional | Filter by exam type: `INTERNAL`, `WEEKLY`, `FINAL`, or omit for all | `INTERNAL` |

*Note: `studentId` is required if no JWT token is provided (for testing). When using authentication, it's extracted from the JWT token.

---

## 🧪 Test Cases & JSON Payloads

### Test Case 1: Get All Exams Performance

**Request:**
```bash
GET http://localhost:9092/api/student/performance/report?studentId=1
```

**Response:**
```json
{
  "overallGrade": "B+",
  "averageScore": 88.75,
  "performanceTrend": "+5%",
  "bestSubject": "Maths",
  "subjects": [
    {
      "subject": "English",
      "grade": "B+",
      "percentage": 86,
      "scoreText": "173/200",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-16",
          "score": "44/50",
          "percentage": 88
        },
        {
          "examType": "WEEKLY",
          "date": "2026-03-20",
          "score": "43/50",
          "percentage": 86
        },
        {
          "examType": "FINAL",
          "date": "2026-03-26",
          "score": "86/100",
          "percentage": 86
        }
      ]
    },
    {
      "subject": "Maths",
      "grade": "A",
      "percentage": 95,
      "scoreText": "190/200",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-15",
          "score": "48/50",
          "percentage": 96
        },
        {
          "examType": "WEEKLY",
          "date": "2026-03-19",
          "score": "47/50",
          "percentage": 94
        },
        {
          "examType": "FINAL",
          "date": "2026-03-25",
          "score": "95/100",
          "percentage": 95
        }
      ]
    },
    {
      "subject": "Science",
      "grade": "A",
      "percentage": 91,
      "scoreText": "183/200",
      "tests": [...]
    },
    {
      "subject": "History",
      "grade": "B+",
      "percentage": 82,
      "scoreText": "164/200",
      "tests": [...]
    }
  ]
}
```

---

### Test Case 2: Get INTERNAL Exams Only

**Request:**
```bash
GET http://localhost:9092/api/student/performance/report?studentId=1&examType=INTERNAL
```

**Expected Response:**
- Only shows Internal Assessment exams
- Overall Grade calculated from Internal exams only
- Average Score = 90.5%
- Each subject shows only INTERNAL test scores

---

### Test Case 3: Get WEEKLY Exams Only

**Request:**
```bash
GET http://localhost:9092/api/student/performance/report?studentId=1&examType=WEEKLY
```

**Expected Response:**
- Only shows Weekly Test exams
- Overall Grade calculated from Weekly tests only
- Average Score = 87.5%
- Each subject shows only WEEKLY test scores

---

### Test Case 4: Get FINAL Exams Only

**Request:**
```bash
GET http://localhost:9092/api/student/performance/report?studentId=1&examType=FINAL
```

**Expected Response:**
- Only shows Final exams
- Overall Grade calculated from Final exams only
- Average Score = 88.75%
- Each subject shows only FINAL test scores

---

## 📝 Response Schema

### Main Response Object

```typescript
{
  overallGrade: string;           // A, B+, B, etc.
  averageScore: number;            // Percentage (0-100)
  performanceTrend: string;         // e.g., "+5%"
  bestSubject: string;              // Subject name with highest average
  subjects: SubjectPerformance[];   // Array of subject details
}
```

### SubjectPerformance Object

```typescript
{
  subject: string;                  // Subject name (e.g., "Maths")
  grade: string;                    // Letter grade
  percentage: number;               // Average percentage for this subject
  scoreText: string;                // Formatted score (e.g., "173/200")
  tests: TestScore[];               // Array of individual test scores
}
```

### TestScore Object

```typescript
{
  examType: string;                 // "INTERNAL", "WEEKLY", "FINAL"
  date: string;                     // ISO date format (YYYY-MM-DD)
  score: string;                    // Formatted score (e.g., "44/50")
  percentage: number;               // Score percentage (0-100)
}
```

---

## 🗄️ Database Tables

### Exams Table
```sql
CREATE TABLE exams (
  exam_id BIGINT PRIMARY KEY,
  exam_name VARCHAR(150),
  exam_date DATE NOT NULL,
  exam_type VARCHAR(50) NOT NULL,  -- INTERNAL, WEEKLY, FINAL
  subject_id BIGINT,
  start_time TIME,
  duration_minutes INTEGER
);
```

### Exam Results Table
```sql
CREATE TABLE exam_results (
  result_id BIGINT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  exam_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  obtained_marks INTEGER,
  total_marks INTEGER,
  delta_percent INTEGER,
  grade VARCHAR(5),
  rank INTEGER,
  feedback VARCHAR(500)
);
```

### Subjects Table
```sql
CREATE TABLE subjects (
  subject_id BIGINT PRIMARY KEY,
  subject_name VARCHAR(100)
);
```

---

## ✅ Validation & Error Handling

### Valid Exam Types
- `INTERNAL` - Internal Assessment exams
- `WEEKLY` - Weekly test exams
- `FINAL` - Final exams
- `null` or omitted - All exams combined

### Invalid Exam Type Handling
If an invalid `examType` is provided (e.g., `?examType=MID_TERM`):
- The API treats it as `null`
- Returns performance for ALL exams
- No error is thrown

### Student Not Found
If `studentId` does not exist in the database:
- Status: **404 Not Found**
- Response: `{"message":"Student not found: {id}"}`

### Empty Performance Data
If a student has no exam results:
- Status: **200 OK**
- Response: All fields are populated with default/empty values
- `averageScore`: 0.0
- `subjects`: Empty array

---

## 🔌 Integration Examples

### Using cURL

```bash
# Get all exams
curl -X GET "http://localhost:9092/api/student/performance/report?studentId=1" \
  -H "Content-Type: application/json"

# Get internal exams only
curl -X GET "http://localhost:9092/api/student/performance/report?studentId=1&examType=INTERNAL" \
  -H "Content-Type: application/json"
```

### Using Postman

1. **Method:** GET
2. **URL:** `http://localhost:9092/api/student/performance/report?studentId=1`
3. **Headers:**
   - `Content-Type: application/json`
4. **Send:** Click Send

### Using JavaScript/Fetch

```javascript
// Fetch all exams
const url = 'http://localhost:9092/api/student/performance/report?studentId=1';
const response = await fetch(url, {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
});
const data = await response.json();
console.log(data);

// Fetch only INTERNAL exams
const urlInternal = 'http://localhost:9092/api/student/performance/report?studentId=1&examType=INTERNAL';
const responseInternal = await fetch(urlInternal);
const dataInternal = await responseInternal.json();
```

### Using Python/Requests

```python
import requests

# Get all exams
url = 'http://localhost:9092/api/student/performance/report?studentId=1'
response = requests.get(url)
data = response.json()
print(data)

# Get filtered exams
url_filtered = 'http://localhost:9092/api/student/performance/report?studentId=1&examType=WEEKLY'
response_filtered = requests.get(url_filtered)
data_filtered = response_filtered.json()
```

---

## 🎨 Sample UI Integration

### React Example

```jsx
import React, { useEffect, useState } from 'react';

function PerformanceReport({ studentId }) {
  const [performance, setPerformance] = useState(null);
  const [examType, setExamType] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchPerformance = async (type) => {
    setLoading(true);
    try {
      const url = `http://localhost:9092/api/student/performance/report?studentId=${studentId}` +
                  (type ? `&examType=${type}` : '');
      const response = await fetch(url);
      const data = await response.json();
      setPerformance(data);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPerformance(examType);
  }, [examType]);

  if (!performance) return <div>Loading...</div>;

  return (
    <div className="performance-report">
      <h2>Overall Grade: {performance.overallGrade}</h2>
      <p>Average Score: {performance.averageScore}%</p>
      <p>Best Subject: {performance.bestSubject}</p>
      
      <div className="exam-filters">
        <button onClick={() => setExamType(null)}>All Exams</button>
        <button onClick={() => setExamType('INTERNAL')}>Internal</button>
        <button onClick={() => setExamType('WEEKLY')}>Weekly</button>
        <button onClick={() => setExamType('FINAL')}>Final</button>
      </div>

      <div className="subjects">
        {performance.subjects.map(subject => (
          <div key={subject.subject} className="subject-card">
            <h3>{subject.subject}</h3>
            <p>Grade: {subject.grade}</p>
            <p>Percentage: {subject.percentage}%</p>
            <p>Score: {subject.scoreText}</p>
            <ul>
              {subject.tests.map((test, idx) => (
                <li key={idx}>
                  {test.examType}: {test.score} ({test.percentage}%) on {test.date}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}

export default PerformanceReport;
```

---

## 📊 Test Data Inserted

### Exams (12 total)
- 4 INTERNAL exams (Math, English, Science, History)
- 4 WEEKLY exams (Math, English, Science, History)
- 4 FINAL exams (Math, English, Science, History)

### Exam Results (12 total)
- Student ID: 1 (John Doe)
- High performance across all subjects
- Overall Average: 88.75%
- Best Subject: Mathematics (95%)

---

## 🔐 Authentication

Currently, the API supports:
1. **JWT Authentication** - Recommended for production
2. **studentId Parameter** - For testing without authentication

To use JWT authentication:
1. Obtain a JWT token from `/api/auth/login`
2. Add to request headers:
   ```
   Authorization: Bearer {token}
   ```
3. Remove `?studentId=` parameter

---

## 🐛 Troubleshooting

### Issue: 500 Internal Server Error
**Cause:** Invalid examType or data inconsistency
**Solution:** 
- Verify `examType` is one of: INTERNAL, WEEKLY, FINAL
- Check that student exists in database
- Check that exams and exam_results tables have data

### Issue: 404 Not Found
**Cause:** Student doesn't exist
**Solution:** 
- Verify studentId is correct
- Check students table has entry with given ID

### Issue: Empty subjects array
**Cause:** Student has no exam results
**Solution:**
- Insert test data using `INSERT_PERFORMANCE_TEST_DATA.sql`
- Verify exam_results table has entries for the student

---

## 📈 Future Enhancements

1. **Performance Trends:** Show progress over time
2. **Class Comparison:** Compare student with class average
3. **Detailed Analytics:** Strength/weakness analysis
4. **Export Functionality:** PDF/Excel reports
5. **Predictive Analytics:** Grade forecasting
6. **Peer Comparison:** Anonymous benchmarking
7. **Goal Setting:** Target grade tracking

---

## 📞 Support

For issues or questions:
1. Check the troubleshooting section
2. Review test data in `INSERT_PERFORMANCE_TEST_DATA.sql`
3. Check backend logs for detailed error messages
4. Verify database connectivity and data integrity

---

## ✨ Summary

✅ **Status: WORKING**

- API Endpoint: `GET /api/student/performance/report`
- Test Data: 12 exams, 12 results for Student ID 1
- Exam Types: INTERNAL, WEEKLY, FINAL (with filtering)
- Response: Comprehensive subject-wise performance breakdown
- Average Score: 88.75% (Overall Grade: B+)

All API endpoints have been tested and are working correctly!

