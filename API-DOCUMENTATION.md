# 📋 Notes API - REST Endpoints

## Base URL
```
http://localhost:8080
```

---

## 🔐 Authentication

### Login
**Endpoint:** `POST /auth/login`

**Input:**
```json
{
  "email": "admin@example.com",
  "password": "1234"
}
```

**Output (Success):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 1800,
  "role": "ADMIN"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}'
```

---

## 📝 Notes Management

### 1. Get All Notes
**Endpoint:** `GET /notes`

**Headers:**
```
Authorization: Bearer <token>
```

**Input:** None

**Output:**
```json
[
  {
    "id": "abc-123",
    "title": "First Note",
    "content": "Note content here"
  },
  {
    "id": "xyz-789",
    "title": "Second Note",
    "content": "Another note"
  }
]
```

**cURL Example:**
```bash
curl http://localhost:8080/notes \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 2. Get Single Note
**Endpoint:** `GET /notes/{id}`

**Headers:**
```
Authorization: Bearer <token>
```

**Input:** `id` in URL path

**Output (Success):**
```json
{
  "id": "abc-123",
  "title": "First Note",
  "content": "Note content here"
}
```

**Output (Not Found):**
```json
{
  "message": "Note not found"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/notes/abc-123 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 3. Create Note (Admin Only)
**Endpoint:** `POST /notes`

**Headers:**
```
Authorization: Bearer <admin_token>
Content-Type: application/json
```

**Input:**
```json
{
  "title": "My New Note",
  "content": "This is the note content"
}
```

**Output (Success):**
```json
{
  "id": "new-uuid-123",
  "title": "My New Note",
  "content": "This is the note content"
}
```

**Output (Forbidden):**
```json
{
  "message": "Admins only"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/notes \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My New Note","content":"This is the note content"}'
```

---

### 4. Delete Note (Admin Only)
**Endpoint:** `DELETE /notes/{id}`

**Headers:**
```
Authorization: Bearer <admin_token>
```

**Input:** `id` in URL path

**Output (Success):**
```json
{
  "message": "Deleted"
}
```

**Output (Not Found):**
```json
{
  "message": "Note not found"
}
```

**Output (Forbidden):**
```json
{
  "message": "Admins only"
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/notes/abc-123 \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

## 📁 File Storage (GCP)

### 1. Upload File (Admin Only)
**Endpoint:** `POST /files/upload`

**Headers:**
```
Authorization: Bearer <admin_token>
Content-Type: multipart/form-data
```

**Input:** File as multipart form data
```
file: [binary file data]
```

**Output (Success):**
```json
{
  "message": "File uploaded successfully",
  "fileName": "document.pdf",
  "url": "https://storage.googleapis.com/notes-pdfs/uuid-document.pdf"
}
```

**Output (Forbidden):**
```json
{
  "message": "Admins only"
}
```

**Output (Error):**
```json
{
  "message": "Upload failed: error details"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/files/upload \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -F "file=@document.pdf"
```

---

### 2. Get Signed URL
**Endpoint:** `GET /files/url/{fileName}`

**Headers:**
```
Authorization: Bearer <token>
```

**Input:** `fileName` in URL path

**Output (Success):**
```json
{
  "fileName": "uuid-document.pdf",
  "url": "https://storage.googleapis.com/notes-pdfs/uuid-document.pdf?X-Goog-Algorithm=...",
  "expiresIn": "1 hour"
}
```

**Output (Not Found):**
```json
{
  "message": "File not found"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/files/url/uuid-document.pdf \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🔑 Authorization Matrix

| Endpoint | Method | Admin | User | Public |
|----------|--------|-------|------|--------|
| `/auth/login` | POST | ✅ | ✅ | ✅ |
| `/notes` | GET | ✅ | ✅ | ❌ |
| `/notes/{id}` | GET | ✅ | ✅ | ❌ |
| `/notes` | POST | ✅ | ❌ | ❌ |
| `/notes/{id}` | DELETE | ✅ | ❌ | ❌ |
| `/files/upload` | POST | ✅ | ❌ | ❌ |
| `/files/url/{fileName}` | GET | ✅ | ✅ | ❌ |

---

## 🧪 Complete Test Flow

```bash
# Step 1: Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"1234"}'

# Response:
# {"accessToken":"eyJhbG...","expiresIn":1800,"role":"ADMIN"}

# Step 2: Create Note
curl -X POST http://localhost:8080/notes \
  -H "Authorization: Bearer eyJhbG..." \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","content":"Hello World"}'

# Response:
# {"id":"abc-123","title":"Test","content":"Hello World"}

# Step 3: Get All Notes
curl http://localhost:8080/notes \
  -H "Authorization: Bearer eyJhbG..."

# Response:
# [{"id":"abc-123","title":"Test","content":"Hello World"}]

# Step 4: Upload File
curl -X POST http://localhost:8080/files/upload \
  -H "Authorization: Bearer eyJhbG..." \
  -F "file=@document.pdf"

# Response:
# {"message":"File uploaded successfully","fileName":"document.pdf","url":"https://..."}

# Step 5: Get Signed URL
curl http://localhost:8080/files/url/uuid-document.pdf \
  -H "Authorization: Bearer eyJhbG..."

# Response:
# {"fileName":"uuid-document.pdf","url":"https://...?X-Goog-Algorithm=...","expiresIn":"1 hour"}
```

---

## 📊 HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful request |
| 403 | Forbidden | Non-admin trying admin endpoint |
| 404 | Not Found | Note/File doesn't exist |
| 500 | Server Error | Upload failed or server error |

---

## 🔐 Test Credentials

**Admin:**
- Email: `admin@example.com`
- Password: `1234`
- Role: `ADMIN`

**User:**
- Email: `user@example.com`
- Password: `1234`
- Role: `USER`

---

## ⚙️ Configuration

**JWT Token:**
- Algorithm: HS256
- Expiry: 30 minutes (1800 seconds)
- Secret: 56 characters minimum

**GCP Storage:**
- Bucket: `notes-pdfs`
- Credentials: `notes-storage-service-key.json`
- Signed URL Expiry: 1 hour

---

## 🎯 Quick Reference

**All Authenticated Endpoints Need:**
```
Authorization: Bearer <your-jwt-token>
```

**JSON Endpoints Need:**
```
Content-Type: application/json
```

**File Upload Needs:**
```
Content-Type: multipart/form-data
```

---

✅ **Simple, Clean REST API Design Complete!**
