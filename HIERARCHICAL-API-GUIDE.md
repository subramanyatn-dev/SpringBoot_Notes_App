# Academic Notes Management System - API Guide

## 🎓 Hierarchical Structure
```
Stream (e.g., CSE, ECE)
  └── Semester (e.g., 1, 2, 3...)
      └── Subject (e.g., Data Structures, Algorithms)
          └── Note (PDF files stored in GCS)
```

## 📁 GCS Storage Path
Files are stored hierarchically:
```
notes-pdfs/
  CSE/
    3/
      DataStructures/
        lecture1.pdf
        lecture2.pdf
```

---

## 🔐 Authentication

### Login (Get JWT Token)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@example.com",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGci...",
  "role": "ADMIN"
}
```

Use this token in all subsequent requests:
```bash
-H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 📚 API Endpoints

### 1️⃣ Stream Endpoints

#### GET /streams - List all streams
```bash
curl http://localhost:8080/streams \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### POST /streams - Create stream (ADMIN only)
```bash
curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "CSE"}'
```

#### DELETE /streams/{id}
```bash
curl -X DELETE http://localhost:8080/streams/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2️⃣ Semester Endpoints

#### GET /streams/{streamId}/semesters - List semesters in a stream
```bash
curl http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### POST /streams/{streamId}/semesters - Create semester (ADMIN only)
```bash
curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 3}'
```

#### DELETE /streams/{streamId}/semesters/{id}
```bash
curl -X DELETE http://localhost:8080/streams/1/semesters/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3️⃣ Subject Endpoints

#### GET /semesters/{semesterId}/subjects - List subjects in a semester
```bash
curl http://localhost:8080/semesters/1/subjects \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### POST /semesters/{semesterId}/subjects - Create subject (ADMIN only)
```bash
curl -X POST http://localhost:8080/semesters/1/subjects \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Data Structures"}'
```

#### DELETE /semesters/{semesterId}/subjects/{id}
```bash
curl -X DELETE http://localhost:8080/semesters/1/subjects/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 4️⃣ Note Endpoints (with File Upload)

#### GET /subjects/{subjectId}/notes - List all notes for a subject
```bash
curl http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### GET /subjects/{subjectId}/notes/{id} - Get specific note
```bash
curl http://localhost:8080/subjects/1/notes/abc-123-uuid \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### POST /subjects/{subjectId}/notes - Upload note PDF (ADMIN only)
```bash
curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "title=Lecture 1 - Introduction" \
  -F "file=@/path/to/lecture1.pdf"
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Lecture 1 - Introduction",
  "fileUrl": "https://storage.googleapis.com/notes-pdfs/CSE/3/DataStructures/lecture1.pdf",
  "subjectId": 1,
  "subjectName": "Data Structures"
}
```

#### DELETE /subjects/{subjectId}/notes/{id}
```bash
curl -X DELETE http://localhost:8080/subjects/1/notes/abc-123-uuid \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🧪 Complete Test Flow

```bash
# 1. Login as admin
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@example.com","password":"admin123"}' \
  | jq -r '.token')

# 2. Create a stream
STREAM_ID=$(curl -s -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"CSE"}' | jq -r '.id')

# 3. Create a semester
SEMESTER_ID=$(curl -s -X POST http://localhost:8080/streams/$STREAM_ID/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number":3}' | jq -r '.id')

# 4. Create a subject
SUBJECT_ID=$(curl -s -X POST http://localhost:8080/semesters/$SEMESTER_ID/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Data Structures"}' | jq -r '.id')

# 5. Upload a note
curl -X POST http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 1 - Introduction" \
  -F "file=@/path/to/your/file.pdf"

# 6. List all notes for the subject
curl http://localhost:8080/subjects/$SUBJECT_ID/notes \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔒 Security Rules

| Role | GET | POST | DELETE |
|------|-----|------|--------|
| **USER** | ✅ Yes | ❌ No | ❌ No |
| **ADMIN** | ✅ Yes | ✅ Yes | ✅ Yes |

---

## 📝 Notes

1. **Note IDs are UUIDs (String)**, not Long integers
2. **Files are uploaded to GCS** with hierarchical paths: `Stream/Semester/Subject/filename`
3. **All authenticated users can view** (GET), only **ADMINs can create/delete**
4. **JWT tokens expire in 30 minutes** (1800 seconds)

---

## ✨ What Changed from Original API

### Before (Flat Structure):
```
POST /notes
GET /notes
GET /notes/{id}
DELETE /notes/{id}
```

### After (Hierarchical Structure):
```
Streams → Semesters → Subjects → Notes

POST /streams
GET /streams/{id}/semesters
POST /semesters/{id}/subjects
POST /subjects/{id}/notes (with file upload)
```

### GCS Storage:
- **Before:** `notes-pdfs/uuid-filename.pdf`
- **After:** `notes-pdfs/CSE/3/DataStructures/filename.pdf`
