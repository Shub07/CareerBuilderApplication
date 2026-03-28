# 🧪 POSTMAN TEST GUIDE - Student Performance Report API

---

## 🔗 API ENDPOINT

```
GET http://localhost:9092/api/student/performance/report
```

**Note:** This is a GET request - no request body needed. Use query parameters instead.

---

## 📋 POSTMAN SETUP

### 1. Create New Request

| Field | Value |
|-------|-------|
| **Method** | GET |
| **URL** | http://localhost:9092/api/student/performance/report |
| **Auth** | None (or inherit from collection) |

### 2. Add Headers (Optional but Recommended)

| Header | Value |
|--------|-------|
| Content-Type | application/json |
| Accept | application/json |

---

## 🧪 TEST CASE 1: All Exams (No Filter)

### Request Configuration in Postman

**Method:** GET

**URL:**
```
http://localhost:9092/api/student/performance/report?studentId=1
```

**Headers:**
```
Content-Type: application/json
Accept: application/json
```

**Params Tab:**
```
studentId = 1
```

**Expected Response (200 OK):**
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
      "subject": "History",
      "grade": "B+",
      "percentage": 82,
      "scoreText": "164/200",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-18",
          "score": "42/50",
          "percentage": 84
        },
        {
          "examType": "WEEKLY",
          "date": "2026-03-22",
          "score": "40/50",
          "percentage": 80
        },
        {
          "examType": "FINAL",
          "date": "2026-03-28",
          "score": "82/100",
          "percentage": 82
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
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-17",
          "score": "46/50",
          "percentage": 92
        },
        {
          "examType": "WEEKLY",
          "date": "2026-03-21",
          "score": "45/50",
          "percentage": 90
        },
        {
          "examType": "FINAL",
          "date": "2026-03-27",
          "score": "92/100",
          "percentage": 92
        }
      ]
    }
  ]
}
```

---

## 🧪 TEST CASE 2: INTERNAL Exams Only

### Request Configuration in Postman

**Method:** GET

**URL:**
```
http://localhost:9092/api/student/performance/report?studentId=1&examType=INTERNAL
```

**Params Tab:**
```
studentId = 1
examType = INTERNAL
```

**Expected Response (200 OK):**
```json
{
  "overallGrade": "A",
  "averageScore": 90.5,
  "performanceTrend": "+5%",
  "bestSubject": "Maths",
  "subjects": [
    {
      "subject": "English",
      "grade": "A",
      "percentage": 88,
      "scoreText": "44/50",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-16",
          "score": "44/50",
          "percentage": 88
        }
      ]
    },
    {
      "subject": "History",
      "grade": "B+",
      "percentage": 84,
      "scoreText": "42/50",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-18",
          "score": "42/50",
          "percentage": 84
        }
      ]
    },
    {
      "subject": "Maths",
      "grade": "A",
      "percentage": 96,
      "scoreText": "48/50",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-15",
          "score": "48/50",
          "percentage": 96
        }
      ]
    },
    {
      "subject": "Science",
      "grade": "A",
      "percentage": 92,
      "scoreText": "46/50",
      "tests": [
        {
          "examType": "INTERNAL",
          "date": "2026-03-17",
          "score": "46/50",
          "percentage": 92
        }
      ]
    }
  ]
}
```

---

## 🧪 TEST CASE 3: WEEKLY Exams Only

### Request Configuration in Postman

**Method:** GET

**URL:**
```
http://localhost:9092/api/student/performance/report?studentId=1&examType=WEEKLY
```

**Params Tab:**
```
studentId = 1
examType = WEEKLY
```

**Expected Response (200 OK):**
```json
{
  "overallGrade": "A",
  "averageScore": 87.5,
  "performanceTrend": "+5%",
  "bestSubject": "Maths",
  "subjects": [
    {
      "subject": "English",
      "grade": "A",
      "percentage": 86,
      "scoreText": "43/50",
      "tests": [
        {
          "examType": "WEEKLY",
          "date": "2026-03-20",
          "score": "43/50",
          "percentage": 86
        }
      ]
    },
    {
      "subject": "History",
      "grade": "B",
      "percentage": 80,
      "scoreText": "40/50",
      "tests": [
        {
          "examType": "WEEKLY",
          "date": "2026-03-22",
          "score": "40/50",
          "percentage": 80
        }
      ]
    },
    {
      "subject": "Maths",
      "grade": "A",
      "percentage": 94,
      "scoreText": "47/50",
      "tests": [
        {
          "examType": "WEEKLY",
          "date": "2026-03-19",
          "score": "47/50",
          "percentage": 94
        }
      ]
    },
    {
      "subject": "Science",
      "grade": "A",
      "percentage": 90,
      "scoreText": "45/50",
      "tests": [
        {
          "examType": "WEEKLY",
          "date": "2026-03-21",
          "score": "45/50",
          "percentage": 90
        }
      ]
    }
  ]
}
```

---

## 🧪 TEST CASE 4: FINAL Exams Only

### Request Configuration in Postman

**Method:** GET

**URL:**
```
http://localhost:9092/api/student/performance/report?studentId=1&examType=FINAL
```

**Params Tab:**
```
studentId = 1
examType = FINAL
```

**Expected Response (200 OK):**
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
      "scoreText": "86/100",
      "tests": [
        {
          "examType": "FINAL",
          "date": "2026-03-26",
          "score": "86/100",
          "percentage": 86
        }
      ]
    },
    {
      "subject": "History",
      "grade": "B+",
      "percentage": 82,
      "scoreText": "82/100",
      "tests": [
        {
          "examType": "FINAL",
          "date": "2026-03-28",
          "score": "82/100",
          "percentage": 82
        }
      ]
    },
    {
      "subject": "Maths",
      "grade": "A",
      "percentage": 95,
      "scoreText": "95/100",
      "tests": [
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
      "percentage": 92,
      "scoreText": "92/100",
      "tests": [
        {
          "examType": "FINAL",
          "date": "2026-03-27",
          "score": "92/100",
          "percentage": 92
        }
      ]
    }
  ]
}
```

---

## 📝 POSTMAN QUICK STEPS

### Step 1: Set Up Collection
1. Open Postman
2. Create new Collection: "Career Builder LMS"
3. Add new Request: "Get Performance Report"

### Step 2: Configure Request
1. **Method:** Select GET
2. **URL:** Paste `http://localhost:9092/api/student/performance/report`
3. **Headers Tab:** Add `Content-Type: application/json`

### Step 3: Add Query Parameters
1. Click **Params** tab
2. Add:
   - Key: `studentId` | Value: `1`
   - Key: `examType` | Value: `INTERNAL` (or leave blank for all)

### Step 4: Send Request
1. Click **Send**
2. View response in **Body** tab
3. Should see 200 OK status

---

## 🔧 POSTMAN ENVIRONMENT SETUP (Optional)

### Create Environment Variable

1. Click **Environments** (left sidebar)
2. Create new environment: "Career Builder"
3. Add variables:

| Variable | Value |
|----------|-------|
| base_url | http://localhost:9092 |
| student_id | 1 |

### Use in Request

**URL becomes:**
```
{{base_url}}/api/student/performance/report?studentId={{student_id}}
```

---

## ⚠️ COMMON ISSUES & SOLUTIONS

| Issue | Solution |
|-------|----------|
| 404 Not Found | Check URL is exactly `http://localhost:9092/api/student/performance/report` |
| 500 Error | Verify backend is running on port 9092 |
| No Response | Check backend logs, restart if needed |
| Invalid examType | Use INTERNAL, WEEKLY, FINAL (case-sensitive) |
| Empty Response | Verify database has test data loaded |

---

## ✅ SUCCESS INDICATORS

✅ Status Code: **200 OK**
✅ Response Type: **application/json**
✅ Has Fields: `overallGrade`, `averageScore`, `bestSubject`, `subjects`
✅ Subjects Array: Not empty (should have 4 subjects)
✅ Each Subject Has: `subject`, `grade`, `percentage`, `scoreText`, `tests`

---

## 📊 RESPONSE SCHEMA

```json
{
  "overallGrade": "string (A, B+, B, etc.)",
  "averageScore": "number (0-100)",
  "performanceTrend": "string (e.g., +5%)",
  "bestSubject": "string (subject name)",
  "subjects": [
    {
      "subject": "string",
      "grade": "string",
      "percentage": "number",
      "scoreText": "string (e.g., 190/200)",
      "tests": [
        {
          "examType": "string (INTERNAL/WEEKLY/FINAL)",
          "date": "string (YYYY-MM-DD)",
          "score": "string (e.g., 48/50)",
          "percentage": "number"
        }
      ]
    }
  ]
}
```

---

## 🎯 PARAMETER OPTIONS

### examType Parameter Values
- **INTERNAL** - Internal Assessment exams
- **WEEKLY** - Weekly Test exams
- **FINAL** - Final exams
- **omit** - All exams combined

### studentId Parameter
- **1** - John Doe (test student with all data)
- **Other IDs** - Will return 404 if student doesn't exist

---

## 💾 SAVE REQUESTS IN POSTMAN

After creating each request:
1. Click **Save**
2. Collection: Select "Career Builder LMS"
3. Request name: e.g., "Performance Report - All Exams"
4. Click **Save**

Now you can run requests anytime!

---

## 🚀 TEST ALL VARIATIONS

Save these 5 requests in your collection:

1. ✅ **Performance Report - All Exams**
   ```
   GET http://localhost:9092/api/student/performance/report?studentId=1
   ```

2. ✅ **Performance Report - INTERNAL Only**
   ```
   GET http://localhost:9092/api/student/performance/report?studentId=1&examType=INTERNAL
   ```

3. ✅ **Performance Report - WEEKLY Only**
   ```
   GET http://localhost:9092/api/student/performance/report?studentId=1&examType=WEEKLY
   ```

4. ✅ **Performance Report - FINAL Only**
   ```
   GET http://localhost:9092/api/student/performance/report?studentId=1&examType=FINAL
   ```

5. ✅ **Performance Report - Default (No Params)**
   ```
   GET http://localhost:9092/api/student/performance/report
   ```

---

## 📝 NOTES

- This is a **GET request** - no request body needed
- Query parameters go in **Params** tab or in URL
- All responses are **JSON** format
- Status code should be **200 OK**
- Response time should be < **200ms**

---

*Ready to test? Open Postman and start with Test Case 1!*

