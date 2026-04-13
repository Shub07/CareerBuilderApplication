# Add Schools via REST API (Alternative Method)

## If you prefer to add schools through the API instead of SQL:

### Step 1: Create School 1 (Delhi Public School)
```bash
POST http://localhost:9091/api/schools
Content-Type: application/json

{
    "schoolName": "Delhi Public School",
    "schoolCode": "DPS001",
    "address": "123 School Lane, Delhi",
    "city": "Delhi",
    "state": "Delhi",
    "country": "India",
    "phone": "9876543200",
    "email": "principal@dps.edu.in",
    "principalName": "Dr. Sharma",
    "establishedYear": "2010"
}
```

### Step 2: Create School 2 (Mumbai Academy)
```bash
POST http://localhost:9091/api/schools
Content-Type: application/json

{
    "schoolName": "Mumbai Academy",
    "schoolCode": "MA001",
    "address": "456 Education Ave, Mumbai",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "phone": "9876543201",
    "email": "principal@mumbaiaca.edu.in",
    "principalName": "Mrs. Patel",
    "establishedYear": "2015"
}
```

### Step 3: Verify Schools Created
```bash
GET http://localhost:9091/api/schools

# You should see both schools in the response with id: 1 and id: 2
```

### Step 4: Now Create Your Vacation
```bash
POST http://localhost:9091/api/vacations/admin/create
Content-Type: application/json

{
    "school_id": 2,
    "vacation_name": "AUTUMN Vacation 2026 S2",
    "vacation_type": "AUTUMN",
    "start_date": "2028-11-15",
    "end_date": "2028-12-15",
    "description": "Extended AUTUMN break for all",
    "is_active": true,
    "created_by": "admin_username"
}
```

## Notes:
- Field names in API requests should match the JSON fields (camelCase)
- The School model expects these fields in JSON format
- Make sure to create schools with id 1 and 2, or note the auto-generated IDs
- Use the returned IDs when creating vacations

