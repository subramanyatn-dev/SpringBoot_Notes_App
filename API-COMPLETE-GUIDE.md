# 📚 Academic Notes Management System - Complete API Documentation

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Academic Notes API                    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Stream (CSE, ECE, Mechanical)                         │
│    └── Semester (1, 2, 3, 4, ...)                      │
│          └── Subject (Data Structures, OS, Networks)    │
│                └── Note (PDF files in GCS)             │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Base URL:** `http://localhost:8080`

**Authentication:** JWT Bearer Token

**File Storage:** Google Cloud Storage (GCS)
- Bucket: `notes-pdfs`
- Path Format: `Stream/Semester/Subject/filename.pdf`
- Example: `CSE/3/Data Structures/lecture1.pdf`

---

## 📋 Table of Contents

1. [Authentication](#authentication)
2. [Stream Management](#stream-management)
3. [Semester Management](#semester-management)
4. [Subject Management](#subject-management)
5. [Note Management (with PDF Upload)](#note-management)
6. [Security & Roles](#security--roles)
7. [Error Handling](#error-handling)
8. [Complete Workflow Examples](#complete-workflow-examples)

---

## 🔐 Authentication

### Login

**Endpoint:** `POST /auth/login`

**Description:** Authenticate user and receive JWT token

**Request Body:**
```json
{
  "email": "admin@example.com",
  "password": "1234"
}
```

**Credentials:**
| Email | Password | Role |
|-------|----------|------|
| `admin@example.com` | `1234` | ADMIN |
| `user@example.com` | `1234` | USER |

**cURL Command:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "1234"
  }'
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFt...",
  "expiresIn": 1800,
  "role": "ADMIN"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials"
}
```

**Save Token for Later Use:**
```bash
# Save token to environment variable
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')

# Verify token is saved
echo $TOKEN
```

**Token Details:**
- **Algorithm:** HS256
- **Expiration:** 30 minutes (1800 seconds)
- **Usage:** Include in `Authorization` header as `Bearer <token>`

---

## 📚 Stream Management

A **Stream** represents an academic program (e.g., CSE, ECE, Mechanical Engineering).

### 1. Create Stream

**Endpoint:** `POST /streams`

**Authorization:** ADMIN only

**Request Body:**
```json
{
  "name": "CSE"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "CSE"}'
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "CSE",
  "semesters": []
}
```

**Examples:**
```bash
# Create multiple streams
curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "ECE"}'

curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Mechanical"}'

curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Civil"}'
```

---

### 2. Get All Streams

**Endpoint:** `GET /streams`

**Authorization:** All authenticated users (USER & ADMIN)

**cURL Command:**
```bash
curl http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "CSE",
    "semesters": []
  },
  {
    "id": 2,
    "name": "ECE",
    "semesters": []
  }
]
```

---

### 3. Get Stream by ID

**Endpoint:** `GET /streams/{id}`

**Authorization:** All authenticated users

**cURL Command:**
```bash
curl http://localhost:8080/streams/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "CSE",
  "semesters": [
    {
      "id": 1,
      "number": 3,
      "subjects": []
    }
  ]
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Stream not found"
}
```

---

### 4. Delete Stream

**Endpoint:** `DELETE /streams/{id}`

**Authorization:** ADMIN only

**cURL Command:**
```bash
curl -X DELETE http://localhost:8080/streams/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "message": "Deleted successfully"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Stream not found"
}
```

**Error Response (403 Forbidden - if USER tries):**
```json
{
  "message": "Admins only"
}
```

---

## 📅 Semester Management

A **Semester** belongs to a Stream and represents a semester number (1, 2, 3, etc.).

### 1. Create Semester

**Endpoint:** `POST /streams/{streamId}/semesters`

**Authorization:** ADMIN only

**Request Body:**
```json
{
  "number": 3
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 3}'
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "number": 3,
  "subjects": []
}
```

**Examples:**
```bash
# Create multiple semesters for CSE (Stream ID: 1)
curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 1}'

curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 2}'

curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 3}'

curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 4}'
```

---

### 2. Get All Semesters in a Stream

**Endpoint:** `GET /streams/{streamId}/semesters`

**Authorization:** All authenticated users

**cURL Command:**
```bash
curl http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "number": 1,
    "subjects": []
  },
  {
    "id": 2,
    "number": 2,
    "subjects": []
  },
  {
    "id": 3,
    "number": 3,
    "subjects": [
      {
        "id": 1,
        "name": "Data Structures",
        "notes": []
      }
    ]
  }
]
```

---

### 3. Get Semester by ID

**Endpoint:** `GET /streams/{streamId}/semesters/{id}`

**Authorization:** All authenticated users

**cURL Command:**
```bash
curl http://localhost:8080/streams/1/semesters/3 \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "id": 3,
  "number": 3,
  "subjects": [
    {
      "id": 1,
      "name": "Data Structures",
      "notes": []
    }
  ]
}
```

---

### 4. Delete Semester

**Endpoint:** `DELETE /streams/{streamId}/semesters/{id}`

**Authorization:** ADMIN only

**cURL Command:**
```bash
curl -X DELETE http://localhost:8080/streams/1/semesters/3 \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "message": "Deleted successfully"
}
```

---

## 📖 Subject Management

A **Subject** belongs to a Semester (e.g., Data Structures, Operating Systems).

### 1. Create Subject

**Endpoint:** `POST /semesters/{semesterId}/subjects`

**Authorization:** ADMIN only

**Request Body:**
```json
{
  "name": "Data Structures"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8080/semesters/1/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Data Structures"}'
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "Data Structures",
  "notes": []
}
```

**Examples:**
```bash
# Create multiple subjects for Semester 3
curl -X POST http://localhost:8080/semesters/3/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Data Structures"}'

curl -X POST http://localhost:8080/semesters/3/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Algorithms"}'

curl -X POST http://localhost:8080/semesters/3/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Operating Systems"}'

curl -X POST http://localhost:8080/semesters/3/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Database Management"}'

curl -X POST http://localhost:8080/semesters/3/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Computer Networks"}'
```

---

### 2. Get All Subjects in a Semester

**Endpoint:** `GET /semesters/{semesterId}/subjects`

**Authorization:** All authenticated users

**cURL Command:**
```bash
curl http://localhost:8080/semesters/3/subjects \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Data Structures",
    "notes": []
  },
  {
    "id": 2,
    "name": "Algorithms",
    "notes": []
  },
  {
    "id": 3,
    "name": "Operating Systems",
    "notes": []
  }
]
```

---

### 3. Get Subject by ID

**Endpoint:** `GET /semesters/{semesterId}/subjects/{id}`

**Authorization:** All authenticated users

**cURL Command:**
```bash
curl http://localhost:8080/semesters/3/subjects/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "name": "Data Structures",
  "notes": [
    {
      "id": "ea2dddc7-f710-408c-932d-5edbf0ddc41b",
      "title": "Lecture 1 - Introduction",
      "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE%2F3%2FData%20Structures%2Flecture1.pdf",
      "subjectId": 1,
      "subjectName": "Data Structures"
    }
  ]
}
```

---

### 4. Delete Subject

**Endpoint:** `DELETE /semesters/{semesterId}/subjects/{id}`

**Authorization:** ADMIN only

**cURL Command:**
```bash
curl -X DELETE http://localhost:8080/semesters/3/subjects/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "message": "Deleted successfully"
}
```

---

## 📄 Note Management

A **Note** is a PDF file uploaded to GCS, belonging to a specific Subject.

### 1. Upload Note (with PDF File)

**Endpoint:** `POST /subjects/{subjectId}/notes`

**Authorization:** ADMIN only

**Content-Type:** `multipart/form-data`

**Form Fields:**
- `title` (string): Title of the note
- `file` (file): PDF file to upload

**cURL Command:**
```bash
curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 1 - Introduction to Data Structures" \
  -F "file=@/path/to/your/file.pdf"
```

**Example with actual file:**
```bash
# Upload a PDF from current directory
curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 1 - Introduction" \
  -F "file=@lecture1.pdf"

# Upload with full path
curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 2 - Arrays and Linked Lists" \
  -F "file=@/Users/username/Documents/lecture2.pdf"
```

**Success Response (200 OK):**
```json
{
  "id": "ea2dddc7-f710-408c-932d-5edbf0ddc41b",
  "title": "Lecture 1 - Introduction",
  "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE%2F3%2FData%20Structures%2Flecture1.pdf",
  "subjectId": 1,
  "subjectName": "Data Structures"
}
```

**GCS Storage Path:**
The file is automatically stored in GCS with hierarchical path:
```
notes-pdfs/
  CSE/
    3/
      Data Structures/
        lecture1.pdf
```

**URL Encoding:**
- Spaces are encoded as `%20`
- Forward slashes as `%2F`
- Example: `CSE%2F3%2FData%20Structures%2Flecture1.pdf`

**Upload Multiple Notes:**
```bash
# Upload multiple lectures for Data Structures
curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 1 - Introduction" \
  -F "file=@lecture1.pdf"

curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 2 - Arrays" \
  -F "file=@lecture2.pdf"

curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 3 - Linked Lists" \
  -F "file=@lecture3.pdf"

curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 4 - Stacks and Queues" \
  -F "file=@lecture4.pdf"
```

---

### 2. Get All Notes for a Subject

**Endpoint:** `GET /subjects/{subjectId}/notes`

**Authorization:** All authenticated users

**cURL Command:**
```bash
curl http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
[
  {
    "id": "ea2dddc7-f710-408c-932d-5edbf0ddc41b",
    "title": "Lecture 1 - Introduction",
    "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE%2F3%2FData%20Structures%2Flecture1.pdf",
    "subjectId": 1,
    "subjectName": "Data Structures"
  },
  {
    "id": "f3b4e5c6-d7e8-49f0-a1b2-c3d4e5f6a7b8",
    "title": "Lecture 2 - Arrays",
    "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE%2F3%2FData%20Structures%2Flecture2.pdf",
    "subjectId": 1,
    "subjectName": "Data Structures"
  }
]
```

**Pretty Print with jq:**
```bash
curl -s http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

### 3. Get Note by ID

**Endpoint:** `GET /subjects/{subjectId}/notes/{id}`

**Authorization:** All authenticated users

**Note:** The `{id}` is a UUID string (not a number)

**cURL Command:**
```bash
curl http://localhost:8080/subjects/1/notes/ea2dddc7-f710-408c-932d-5edbf0ddc41b \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "id": "ea2dddc7-f710-408c-932d-5edbf0ddc41b",
  "title": "Lecture 1 - Introduction",
  "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE%2F3%2FData%20Structures%2Flecture1.pdf",
  "subjectId": 1,
  "subjectName": "Data Structures"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Note not found"
}
```

---

### 4. Delete Note

**Endpoint:** `DELETE /subjects/{subjectId}/notes/{id}`

**Authorization:** ADMIN only

**Note:** Currently deletes only the database record, not the GCS file

**cURL Command:**
```bash
curl -X DELETE http://localhost:8080/subjects/1/notes/ea2dddc7-f710-408c-932d-5edbf0ddc41b \
  -H "Authorization: Bearer $TOKEN"
```

**Success Response (200 OK):**
```json
{
  "message": "Deleted successfully"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Note not found"
}
```

---

## 🔒 Security & Roles

### Role-Based Access Control

| Endpoint | USER | ADMIN |
|----------|------|-------|
| `POST /auth/login` | ✅ Public | ✅ Public |
| `GET /streams/**` | ✅ Yes | ✅ Yes |
| `POST /streams` | ❌ No | ✅ Yes |
| `DELETE /streams/**` | ❌ No | ✅ Yes |
| `GET /streams/*/semesters/**` | ✅ Yes | ✅ Yes |
| `POST /streams/*/semesters` | ❌ No | ✅ Yes |
| `DELETE /streams/*/semesters/**` | ❌ No | ✅ Yes |
| `GET /semesters/*/subjects/**` | ✅ Yes | ✅ Yes |
| `POST /semesters/*/subjects` | ❌ No | ✅ Yes |
| `DELETE /semesters/*/subjects/**` | ❌ No | ✅ Yes |
| `GET /subjects/*/notes/**` | ✅ Yes | ✅ Yes |
| `POST /subjects/*/notes` | ❌ No | ✅ Yes |
| `DELETE /subjects/*/notes/**` | ❌ No | ✅ Yes |

### Test USER Role (Read-Only)

```bash
# Login as USER
USER_TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"1234"}' \
  | jq -r '.accessToken')

# USER can GET (should work)
curl http://localhost:8080/streams \
  -H "Authorization: Bearer $USER_TOKEN"

curl http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $USER_TOKEN"

# USER CANNOT POST (should return 403 Forbidden)
curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Stream"}'
```

**Expected Response (403 Forbidden):**
```json
{
  "message": "Admins only"
}
```

---

## ⚠️ Error Handling

### Common Error Responses

#### 401 Unauthorized (Invalid/Missing Token)
```json
{
  "message": "Invalid or expired token"
}
```

**Cause:** Token is missing, expired, or invalid

**Solution:**
```bash
# Get a fresh token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')
```

---

#### 403 Forbidden (Insufficient Permissions)
```json
{
  "message": "Admins only"
}
```

**Cause:** USER role trying to perform ADMIN-only action

**Solution:** Login with admin credentials

---

#### 404 Not Found
```json
{
  "message": "Stream not found"
}
```

**Cause:** Resource with given ID doesn't exist

**Solution:** Verify the ID exists by listing all resources first

---

#### 400 Bad Request
```json
{
  "message": "Name is required"
}
```

**Cause:** Required field missing in request body

**Solution:** Check the request body matches the expected format

---

#### 500 Internal Server Error
```json
{
  "message": "Error message here"
}
```

**Cause:** Server-side error (GCS upload failed, database error, etc.)

**Solution:** Check server logs for details

---

## 🧪 Complete Workflow Examples

### Example 1: Full Setup for CSE Semester 3

```bash
# Step 1: Login as Admin
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')

echo "✅ Logged in. Token: $TOKEN"

# Step 2: Create Stream
STREAM_RESPONSE=$(curl -s -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"CSE"}')

STREAM_ID=$(echo $STREAM_RESPONSE | jq -r '.id')
echo "✅ Created Stream: CSE (ID: $STREAM_ID)"

# Step 3: Create Semester 3
SEMESTER_RESPONSE=$(curl -s -X POST http://localhost:8080/streams/$STREAM_ID/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number":3}')

SEMESTER_ID=$(echo $SEMESTER_RESPONSE | jq -r '.id')
echo "✅ Created Semester: 3 (ID: $SEMESTER_ID)"

# Step 4: Create Subject - Data Structures
SUBJECT_RESPONSE=$(curl -s -X POST http://localhost:8080/semesters/$SEMESTER_ID/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Data Structures"}')

SUBJECT_ID=$(echo $SUBJECT_RESPONSE | jq -r '.id')
echo "✅ Created Subject: Data Structures (ID: $SUBJECT_ID)"

# Step 5: Upload Notes
NOTE1=$(curl -s -X POST http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 1 - Introduction" \
  -F "file=@lecture1.pdf")

echo "✅ Uploaded Note: Lecture 1"

NOTE2=$(curl -s -X POST http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 2 - Arrays" \
  -F "file=@lecture2.pdf")

echo "✅ Uploaded Note: Lecture 2"

# Step 6: View All Notes
echo "\n📚 All Notes for Data Structures:"
curl -s http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

### Example 2: Complete CSE Program (8 Semesters)

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')

# Create Stream
STREAM_ID=$(curl -s -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"CSE"}' | jq -r '.id')

# Create 8 Semesters
for i in {1..8}; do
  curl -s -X POST http://localhost:8080/streams/$STREAM_ID/semesters \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"number\":$i}" > /dev/null
  echo "✅ Created Semester $i"
done

# Get Semester 3 ID
SEM3_ID=$(curl -s http://localhost:8080/streams/$STREAM_ID/semesters \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.[] | select(.number == 3) | .id')

# Create Subjects for Semester 3
SUBJECTS=("Data Structures" "Algorithms" "Operating Systems" "Database Management" "Computer Networks")

for subject in "${SUBJECTS[@]}"; do
  curl -s -X POST http://localhost:8080/semesters/$SEM3_ID/subjects \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$subject\"}" > /dev/null
  echo "✅ Created Subject: $subject"
done

# View Complete Structure
echo "\n📚 Complete CSE Structure:"
curl -s http://localhost:8080/streams/$STREAM_ID \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

### Example 3: Bulk Upload Notes

```bash
# Assume we have Subject ID = 1 (Data Structures)
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')

SUBJECT_ID=1

# Upload multiple PDFs from a folder
for i in {1..10}; do
  if [ -f "lecture$i.pdf" ]; then
    curl -s -X POST http://localhost:8080/subjects/$SUBJECT_ID/notes \
      -H "Authorization: Bearer $TOKEN" \
      -F "title=Lecture $i" \
      -F "file=@lecture$i.pdf" > /dev/null
    echo "✅ Uploaded Lecture $i"
  fi
done

# View all uploaded notes
echo "\n📄 All Uploaded Notes:"
curl -s http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[] | "[\(.id)] \(.title)"'
```

---

### Example 4: Search and Filter (Using jq)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')

# Get all streams and count them
echo "Total Streams:"
curl -s http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" | jq 'length'

# Find stream ID by name
CSE_ID=$(curl -s http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.[] | select(.name == "CSE") | .id')

echo "CSE Stream ID: $CSE_ID"

# Get all subjects in Semester 3
SEM3_ID=$(curl -s http://localhost:8080/streams/$CSE_ID/semesters \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.[] | select(.number == 3) | .id')

echo "\nSubjects in Semester 3:"
curl -s http://localhost:8080/semesters/$SEM3_ID/subjects \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.[] | "- \(.name) (ID: \(.id))"'

# Count total notes across all subjects
TOTAL_NOTES=0
for subject_id in $(curl -s http://localhost:8080/semesters/$SEM3_ID/subjects \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[].id'); do
  
  COUNT=$(curl -s http://localhost:8080/subjects/$subject_id/notes \
    -H "Authorization: Bearer $TOKEN" | jq 'length')
  
  TOTAL_NOTES=$((TOTAL_NOTES + COUNT))
done

echo "\nTotal Notes in Semester 3: $TOTAL_NOTES"
```

---

### Example 5: Delete Hierarchy (Bottom-Up)

```bash
# To delete everything, start from bottom (Notes) to top (Stream)

TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}' \
  | jq -r '.accessToken')

STREAM_ID=1
SEMESTER_ID=1
SUBJECT_ID=1

# Step 1: Delete all notes in subject
NOTE_IDS=$(curl -s http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[].id')

for note_id in $NOTE_IDS; do
  curl -s -X DELETE http://localhost:8080/subjects/$SUBJECT_ID/notes/$note_id \
    -H "Authorization: Bearer $TOKEN" > /dev/null
  echo "❌ Deleted Note: $note_id"
done

# Step 2: Delete subject
curl -X DELETE http://localhost:8080/semesters/$SEMESTER_ID/subjects/$SUBJECT_ID \
  -H "Authorization: Bearer $TOKEN"
echo "❌ Deleted Subject: $SUBJECT_ID"

# Step 3: Delete semester
curl -X DELETE http://localhost:8080/streams/$STREAM_ID/semesters/$SEMESTER_ID \
  -H "Authorization: Bearer $TOKEN"
echo "❌ Deleted Semester: $SEMESTER_ID"

# Step 4: Delete stream
curl -X DELETE http://localhost:8080/streams/$STREAM_ID \
  -H "Authorization: Bearer $TOKEN"
echo "❌ Deleted Stream: $STREAM_ID"
```

---

## 🌐 Frontend Integration Tips

### JavaScript/TypeScript Example

```javascript
// API Base URL
const API_URL = 'http://localhost:8080';

// Login and get token
async function login(email, password) {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  
  const data = await response.json();
  localStorage.setItem('token', data.accessToken);
  return data;
}

// Get all streams
async function getStreams() {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_URL}/streams`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return response.json();
}

// Upload note with file
async function uploadNote(subjectId, title, file) {
  const token = localStorage.getItem('token');
  const formData = new FormData();
  formData.append('title', title);
  formData.append('file', file);
  
  const response = await fetch(`${API_URL}/subjects/${subjectId}/notes`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData
  });
  
  return response.json();
}
```

### React Example (Axios)

```jsx
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
});

// Add token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Login
export const login = async (email, password) => {
  const { data } = await api.post('/auth/login', { email, password });
  localStorage.setItem('token', data.accessToken);
  return data;
};

// Get streams
export const getStreams = () => api.get('/streams');

// Upload note
export const uploadNote = (subjectId, title, file) => {
  const formData = new FormData();
  formData.append('title', title);
  formData.append('file', file);
  return api.post(`/subjects/${subjectId}/notes`, formData);
};
```

---

## 📊 Data Models

### Stream
```json
{
  "id": 1,
  "name": "CSE",
  "semesters": [/* Semester objects */]
}
```

### Semester
```json
{
  "id": 1,
  "number": 3,
  "subjects": [/* Subject objects */]
}
```

### Subject
```json
{
  "id": 1,
  "name": "Data Structures",
  "notes": [/* Note objects */]
}
```

### Note
```json
{
  "id": "ea2dddc7-f710-408c-932d-5edbf0ddc41b",
  "title": "Lecture 1 - Introduction",
  "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE%2F3%2FData%20Structures%2Flecture1.pdf",
  "subjectId": 1,
  "subjectName": "Data Structures"
}
```

---

## 🎯 Quick Reference Commands

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"admin@example.com","password":"1234"}' | jq -r '.accessToken')

# Create Stream
curl -X POST http://localhost:8080/streams -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"CSE"}'

# Create Semester
curl -X POST http://localhost:8080/streams/1/semesters -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"number":3}'

# Create Subject
curl -X POST http://localhost:8080/semesters/1/subjects -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Data Structures"}'

# Upload Note
curl -X POST http://localhost:8080/subjects/1/notes -H "Authorization: Bearer $TOKEN" -F "title=Lecture 1" -F "file=@file.pdf"

# Get All Notes
curl http://localhost:8080/subjects/1/notes -H "Authorization: Bearer $TOKEN"

# Delete Note
curl -X DELETE http://localhost:8080/subjects/1/notes/{uuid} -H "Authorization: Bearer $TOKEN"
```

---

## ✅ System Requirements

- **Java:** 21+
- **Spring Boot:** 3.3.4
- **MySQL:** 8.0+
- **Google Cloud Storage:** Active bucket with credentials
- **jq:** (Optional) For JSON parsing in terminal

---

## 📞 Support

For issues or questions:
1. Check server logs: `mvn spring-boot:run`
2. Verify GCS credentials: `notes-storage-service-key.json`
3. Confirm MySQL connection: `application.properties`

---

**🎉 Happy Coding! Your backend is ready for frontend integration!**
