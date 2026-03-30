# 🧪 EXAM APIs - POSTMAN TEST GUIDE WITH SAMPLE DATA

**Status:** Ready to Test
**Data:** 3 students with 12 exam results
**Endpoints:** 3 main endpoints

---

## 📊 TEST DATA SUMMARY

| Student | ID | Results | Role |
|---------|----|---------| -----|
| John Doe | 1 | 4 exams (2 upcoming, 2 completed) | Average performer |
| Jane Smith | 2 | 4 exams (2 upcoming, 2 completed) | High performer |
| Bob Wilson | 3 | 4 exams (2 upcoming, 2 completed) | Average performer |

**Total:** 12 exam results, 60 question answers, 6 feedback comments

---

## 🔌 ENDPOINT 1: Upcoming Exams

### Request
```
GET http://localhost:9092/api/student/exams/upcoming?studentId=1

Headers:
Content-Type: application/json
Accept: application/json
```

### Response (200 OK)
```json
[
  {
    "exam_id": 1,
    "subject_name": "Mathematics",
    "exam_name": "Unit Test - 1",
    "exam_date": "2026-12-08",
    "start_time": "10:00:00",
    "duration_hours": 2,
    "duration_text": "2 hours",
    "topics": "Topics",
    "exam_type": "Written",
    "days_remaining": 254,
    "is_today": false,
    "status": "In 254 days"
  },
  {
    "exam_id": 2,
    "subject_name": "Physics",
    "exam_name": "Mid-Term Exam",
    "exam_date": "2026-12-15",
    "start_time": "10:00:00",
    "duration_hours": 2,
    "duration_text": "2 hours",
    "topics": "Topics",
    "exam_type": "Written",
    "days_remaining": 261,
    "is_today": false,
    "status": "In 261 days"
  }
]
```

### Test with Different Students

**Test 1: Student 1 (John Doe)**
```
GET http://localhost:9092/api/student/exams/upcoming?studentId=1
Expected: 2 upcoming exams
```

**Test 2: Student 2 (Jane Smith)**
```
GET http://localhost:9092/api/student/exams/upcoming?studentId=2
Expected: 2 upcoming exams
```

**Test 3: Student 3 (Bob Wilson)**
```
GET http://localhost:9092/api/student/exams/upcoming?studentId=3
Expected: 2 upcoming exams
```

---

## 🔌 ENDPOINT 2: Completed Exams

### Request
```
GET http://localhost:9092/api/student/exams/completed?studentId=1

Headers:
Content-Type: application/json
Accept: application/json
```

### Response (200 OK)
```json
[
  {
    "exam_id": 4,
    "subject_name": "Mathematics",
    "exam_name": "Monthly Test",
    "score": "92/100",
    "percentage": 92,
    "grade": "Grade A",
    "exam_date": "Nov 5, 2025",
    "subject_icon": "icon-mathematics"
  },
  {
    "exam_id": 5,
    "subject_name": "Physics",
    "exam_name": "Weekly Assessment",
    "score": "78/100",
    "percentage": 78,
    "grade": "Grade B+",
    "exam_date": "Nov 12, 2025",
    "subject_icon": "icon-physics"
  }
]
```

### Test with Different Students

**Test 1: Student 1 (John Doe) - Average performer**
```
GET http://localhost:9092/api/student/exams/completed?studentId=1
Expected: 
  - Exam 1: 92/100, Grade A
  - Exam 2: 78/100, Grade B+
```

**Test 2: Student 2 (Jane Smith) - High performer**
```
GET http://localhost:9092/api/student/exams/completed?studentId=2
Expected:
  - Exam 1: 95/100, Grade A (Rank 1)
  - Exam 2: 82/100, Grade B (Rank 9)
```

**Test 3: Student 3 (Bob Wilson) - Lower performer**
```
GET http://localhost:9092/api/student/exams/completed?studentId=3
Expected:
  - Exam 1: 80/100, Grade B
  - Exam 2: 72/100, Grade C
```

---

## 🔌 ENDPOINT 3: Exam Result Detail

### Request
```
GET http://localhost:9092/api/student/exams/result/3?studentId=1

Headers:
Content-Type: application/json
Accept: application/json

Note: 
- Path parameter: examResultId = 3 (Student 1, Exam 4)
- Query parameter: studentId = 1 (for verification)
```

### Response (200 OK)
```json
{
  "exam_id": 4,
  "subject_name": "Mathematics",
  "exam_name": "Monthly Test",
  "exam_date": "Nov 5, 2025",
  "total_score": "92/100",
  "percentage": 92,
  "grade": "Grade A",
  
  "marks_breakdown": [
    {
      "section_name": "Section A: MCQ",
      "obtained_marks": 18,
      "total_marks": 20,
      "percentage": 90.0,
      "visual_percentage": 90
    },
    {
      "section_name": "Section B: Short Answer",
      "obtained_marks": 36,
      "total_marks": 40,
      "percentage": 90.0,
      "visual_percentage": 90
    },
    {
      "section_name": "Section C: Long Answer",
      "obtained_marks": 38,
      "total_marks": 40,
      "percentage": 95.0,
      "visual_percentage": 95
    }
  ],
  
  "performance_insights": {
    "class_average": 78,
    "highest_score": 98,
    "your_rank": 5,
    "total_students": 30,
    "rank_text": "#5",
    "performance_label": "Excellent",
    "comparison_with_average": 14,
    "is_above_average": true
  },
  
  "teacher_feedback": {
    "teacher_name": "Mrs. Lucia",
    "teacher_id": 1,
    "feedback_text": "Excellent performance. Focus more on proof-based questions in the future.",
    "feedback_date": "Nov 5, 2025",
    "is_available": true
  },
  
  "question_wise_analysis": [
    {
      "question_number": 1,
      "question_text": "What is the SI unit of force?",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 5,
      "total_marks": 5,
      "section": "Section A: MCQ",
      "is_correct": true
    },
    {
      "question_number": 2,
      "question_text": "What is the formula for kinetic energy?",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 5,
      "total_marks": 5,
      "section": "Section A: MCQ",
      "is_correct": true
    },
    {
      "question_number": 3,
      "question_text": "Define velocity",
      "status": "PARTIAL",
      "status_label": "Partial",
      "marks_obtained": 4,
      "total_marks": 5,
      "section": "Section A: MCQ",
      "is_correct": false
    },
    {
      "question_number": 4,
      "question_text": "What is acceleration?",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 4,
      "total_marks": 5,
      "section": "Section A: MCQ",
      "is_correct": true
    },
    {
      "question_number": 5,
      "question_text": "Explain the first law of motion",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 8,
      "total_marks": 8,
      "section": "Section B: Short Answer",
      "is_correct": true
    },
    {
      "question_number": 6,
      "question_text": "Explain the second law of motion",
      "status": "PARTIAL",
      "status_label": "Partial",
      "marks_obtained": 7,
      "total_marks": 8,
      "section": "Section B: Short Answer",
      "is_correct": false
    },
    {
      "question_number": 7,
      "question_text": "Explain the third law of motion",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 8,
      "total_marks": 8,
      "section": "Section B: Short Answer",
      "is_correct": true
    },
    {
      "question_number": 8,
      "question_text": "What is momentum?",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 8,
      "total_marks": 8,
      "section": "Section B: Short Answer",
      "is_correct": true
    },
    {
      "question_number": 9,
      "question_text": "Derive the equations of motion",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 18,
      "total_marks": 20,
      "section": "Section C: Long Answer",
      "is_correct": true
    },
    {
      "question_number": 10,
      "question_text": "Explain energy conservation principle",
      "status": "CORRECT",
      "status_label": "Correct",
      "marks_obtained": 20,
      "total_marks": 20,
      "section": "Section C: Long Answer",
      "is_correct": true
    }
  ]
}
```

### Test All Result Details

**Test 1: Student 1 - Result 3 (Exam 4 - Monthly Test)**
```
GET http://localhost:9092/api/student/exams/result/3?studentId=1
Expected:
  - Score: 92/100
  - Grade: Grade A
  - Rank: #5
  - Marks breakdown: 90%, 90%, 95%
  - Questions: 10 (9 Correct, 1 Partial)
```

**Test 2: Student 1 - Result 4 (Exam 5 - Weekly Assessment)**
```
GET http://localhost:9092/api/student/exams/result/4?studentId=1
Expected:
  - Score: 78/100
  - Grade: Grade B+
  - Rank: #15
  - Marks breakdown: 75%, 80%, 77.5%
  - Questions: Mixed results
```

**Test 3: Student 2 - Result 5 (Exam 4 - Monthly Test - High Performer)**
```
GET http://localhost:9092/api/student/exams/result/5?studentId=2
Expected:
  - Score: 95/100
  - Grade: Grade A
  - Rank: #1
  - Marks breakdown: 95%, 95%, 95%
  - Questions: 10 Correct, 1 Partial
```

**Test 4: Student 3 - Result 7 (Exam 4 - Monthly Test - Lower Performer)**
```
GET http://localhost:9092/api/student/exams/result/7?studentId=3
Expected:
  - Score: 80/100
  - Grade: Grade B
  - Rank: #7
  - Marks breakdown: 80%, 80%, 80%
  - Questions: Mixed results
```

---

## 📋 POSTMAN COLLECTION SETUP

### Create 6 Requests in Postman

**Request 1: Upcoming Exams - Student 1**
- Method: GET
- URL: `http://localhost:9092/api/student/exams/upcoming?studentId=1`

**Request 2: Upcoming Exams - Student 2**
- Method: GET
- URL: `http://localhost:9092/api/student/exams/upcoming?studentId=2`

**Request 3: Completed Exams - Student 1**
- Method: GET
- URL: `http://localhost:9092/api/student/exams/completed?studentId=1`

**Request 4: Completed Exams - Student 2**
- Method: GET
- URL: `http://localhost:9092/api/student/exams/completed?studentId=2`

**Request 5: Result Detail - Student 1, Result 3**
- Method: GET
- URL: `http://localhost:9092/api/student/exams/result/3?studentId=1`

**Request 6: Result Detail - Student 2, Result 5**
- Method: GET
- URL: `http://localhost:9092/api/student/exams/result/5?studentId=2`

---

## ✅ VALIDATION TESTS

### Test Cases to Verify

1. ✅ **Upcoming exams return future dates**
   - Check that all exam_date > today

2. ✅ **Completed exams return past results**
   - Check that all results have grades and ranks

3. ✅ **Marks breakdown adds up correctly**
   - Section A + B + C = Total Score

4. ✅ **Performance insights calculated correctly**
   - Class average should be between lowest and highest
   - Rank should be sequential

5. ✅ **Teacher feedback is populated**
   - Feedback text should be present
   - Teacher name should match

6. ✅ **Question-wise analysis complete**
   - All 10 questions should be present
   - Status should be CORRECT, WRONG, or PARTIAL

---

## 🚀 DEPLOYMENT STEPS

### Step 1: Run the SQL Script
```bash
psql -U postgres -d admindb -f INSERT_EXAM_TEST_DATA.sql
```

### Step 2: Verify Data Inserted
```bash
psql -U postgres -d admindb -c "SELECT COUNT(*) FROM exam_results;"
# Should return: 12
```

### Step 3: Start Backend
```bash
java -jar target/career-builder-0.0.1-SNAPSHOT.jar --server.port=9092
```

### Step 4: Open Postman
- Import `Career_Builder_Exams_API.postman_collection.json`
- Or create requests manually as shown above

### Step 5: Test Each Endpoint
- Click Send on each request
- Verify 200 OK response
- Check response structure matches examples

---

## 📊 SAMPLE RESULT IDs

| Result ID | Student | Exam | Date | Type |
|-----------|---------|------|------|------|
| 1 | John Doe | Exam 1 | Future | Upcoming |
| 2 | John Doe | Exam 2 | Future | Upcoming |
| 3 | John Doe | Exam 4 | Past | Completed |
| 4 | John Doe | Exam 5 | Past | Completed |
| 5 | Jane Smith | Exam 1 | Future | Upcoming |
| 6 | Jane Smith | Exam 2 | Future | Upcoming |
| 7 | Jane Smith | Exam 4 | Past | Completed |
| 8 | Jane Smith | Exam 5 | Past | Completed |
| 9 | Bob Wilson | Exam 1 | Future | Upcoming |
| 10 | Bob Wilson | Exam 2 | Future | Upcoming |
| 11 | Bob Wilson | Exam 4 | Past | Completed |
| 12 | Bob Wilson | Exam 5 | Past | Completed |

---

## 🎯 SUCCESS CRITERIA

✅ All requests return 200 OK
✅ Response structure matches expected JSON
✅ Data for 3 students available
✅ Both upcoming and completed exams show correctly
✅ Result detail shows all sections (breakdown, insights, feedback, questions)
✅ Marks calculation is accurate
✅ Performance metrics are reasonable

---

**Ready to test! Start with Step 1 of Deployment Steps.** 🚀

