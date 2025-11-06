# 🚀 First Time Cloud SQL Setup Guide

## 📋 Current Status

Your Cloud SQL database `notesdb` exists but is **EMPTY**:
- ❌ No tables created yet
- ❌ No users (admin/user accounts)
- ❌ No data

## ✅ Step-by-Step Setup

### **Step 1: Start Spring Boot Application**

This will automatically create all tables in Cloud SQL:

```bash
cd /Users/subramanyatn/Documents/notes
mvn clean spring-boot:run
```

**What happens:**
- ✅ Connects to Cloud SQL (`34.10.105.237:3306`)
- ✅ Creates tables: `users`, `streams`, `semesters`, `subjects`, `notes`
- ✅ Sets up relationships and indexes
- ✅ Server starts on `http://localhost:8080`

**Look for these logs:**
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Hibernate: create table if not exists streams...
Hibernate: create table if not exists users...
```

Wait for: `Started NotesApplication in X seconds`

---

### **Step 2: Verify Tables Created**

In your **MySQL terminal** (keep it open):

```sql
SHOW TABLES;
```

You should see:
```
+-------------------+
| Tables_in_notesdb |
+-------------------+
| notes             |
| semesters         |
| streams           |
| subjects          |
| users             |
+-------------------+
```

Check if any users exist:
```sql
SELECT * FROM users;
```

Should be **empty** (no rows).

---

### **Step 3: Register a Regular USER Account**

Open a **new terminal** and run:

```bash
# Register a regular USER account
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "testuser@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "message": "User registered successfully",
  "role": "USER"
}
```

---

### **Step 4: Create an ADMIN Account (Direct Database)**

Since the registration API only creates USER accounts, you need to add ADMIN directly to the database.

**Option A: Via MySQL CLI** (In your MySQL terminal):

```sql
INSERT INTO users (name, email, password, role) 
VALUES ('Admin User', 'admin@unifeed.com', 'admin123', 'ADMIN');
```

**Option B: Via curl + SQL**:

```bash
mysql -h 34.10.105.237 -u notes_user -pNotes@123 notesdb -e "
INSERT INTO users (name, email, password, role) 
VALUES ('Admin User', 'admin@unifeed.com', 'admin123', 'ADMIN');
"
```

---

### **Step 5: Login and Get JWT Token**

#### Login as ADMIN:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@unifeed.com",
    "password": "admin123"
  }'
```

**Save the token:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@unifeed.com","password":"admin123"}' \
  | jq -r '.accessToken')

echo "Token: $TOKEN"
```

---

### **Step 6: Create Academic Hierarchy**

Now you can use the ADMIN token to create data:

#### Create a Stream (CSE):
```bash
curl -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CSE"
  }'
```

#### Create a Semester:
```bash
curl -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "number": 3
  }'
```

#### Create a Subject:
```bash
curl -X POST http://localhost:8080/semesters/1/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Data Structures"
  }'
```

#### Upload a Note (PDF):
```bash
curl -X POST http://localhost:8080/subjects/1/notes \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=Lecture 1 - Introduction" \
  -F "file=@/path/to/your/lecture.pdf"
```

---

### **Step 7: Verify in Database**

In your **MySQL terminal**:

```sql
-- See all users
SELECT * FROM users;

-- See all streams
SELECT * FROM streams;

-- See all semesters
SELECT * FROM semesters;

-- See all subjects
SELECT * FROM subjects;

-- See all notes
SELECT * FROM notes;

-- Count everything
SELECT 
  (SELECT COUNT(*) FROM users) as users,
  (SELECT COUNT(*) FROM streams) as streams,
  (SELECT COUNT(*) FROM semesters) as semesters,
  (SELECT COUNT(*) FROM subjects) as subjects,
  (SELECT COUNT(*) FROM notes) as notes;
```

---

## 🎯 Quick Start Script

Save this as `setup_cloud_sql.sh`:

```bash
#!/bin/bash

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🚀 Setting up Cloud SQL Database${NC}"

# Wait for Spring Boot
echo "Waiting for Spring Boot to start..."
sleep 10

# Create ADMIN user in database
echo -e "${GREEN}Creating ADMIN user...${NC}"
mysql -h 34.10.105.237 -u notes_user -pNotes@123 notesdb -e "
INSERT INTO users (name, email, password, role) 
VALUES ('Admin User', 'admin@unifeed.com', 'admin123', 'ADMIN');
" 2>/dev/null

# Get token
echo -e "${GREEN}Getting JWT token...${NC}"
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@unifeed.com","password":"admin123"}' \
  | jq -r '.accessToken')

echo "Token: $TOKEN"

# Create CSE stream
echo -e "${GREEN}Creating CSE stream...${NC}"
curl -s -X POST http://localhost:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "CSE"}' | jq

# Create Semester 3
echo -e "${GREEN}Creating Semester 3...${NC}"
curl -s -X POST http://localhost:8080/streams/1/semesters \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 3}' | jq

# Create Data Structures subject
echo -e "${GREEN}Creating Data Structures subject...${NC}"
curl -s -X POST http://localhost:8080/semesters/1/subjects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Data Structures"}' | jq

echo -e "${BLUE}✅ Setup complete!${NC}"
echo "ADMIN credentials: admin@unifeed.com / admin123"
```

Run it:
```bash
chmod +x setup_cloud_sql.sh
./setup_cloud_sql.sh
```

---

## 📝 Summary

1. **Start Spring Boot** → Creates tables
2. **Create ADMIN user** → Direct SQL INSERT
3. **Login** → Get JWT token
4. **Create data** → Use curl with Bearer token
5. **Verify** → Check in MySQL

---

## 🎓 Default Accounts

After setup you'll have:

**ADMIN Account:**
- Email: `admin@unifeed.com`
- Password: `admin123`
- Role: `ADMIN` (full CRUD access)

**USER Account (if you register):**
- Email: `testuser@example.com`
- Password: `password123`
- Role: `USER` (read-only access)

---

## 🔥 Ready to start!

1. Open Terminal 1: `mvn spring-boot:run`
2. Keep MySQL terminal open
3. Open Terminal 2: Run curl commands above

**Your data is now in the cloud!** ☁️🎉
