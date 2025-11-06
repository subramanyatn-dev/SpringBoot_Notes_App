# 🚀 Upload to GCP VM - Simplified Guide (No gcloud CLI needed)

## ✅ What You Have:
- ✅ VM created: `notes-app-vm`
- ✅ Firewall rule for port 8080
- ✅ JAR file built: `notes-0.0.1-SNAPSHOT.jar` (92MB)
- ✅ Service key: `notes-storage-service-key.json`

## 📤 Method: Upload via GCP Console (Browser)

###Step 1: Open SSH in Browser
1. Go to: https://console.cloud.google.com/compute/instances?project=inbound-fulcrum-475913-n9
2. Find your VM: `notes-app-vm`
3. Click the **"SSH"** button (opens a browser terminal)

### Step 2: Install Java on VM
In the SSH terminal, run:
```bash
# Update system
sudo apt update

# Install Java 21
sudo apt install -y openjdk-21-jdk

# Verify
java -version
```

### Step 3: Upload Files (2 Methods)

#### **Method A: Drag & Drop in SSH Window** (Easiest!)
1. In the SSH browser window, click the ⚙️ (settings) icon → "Upload file"
2. Upload `notes-0.0.1-SNAPSHOT.jar` from `/Users/subramanyatn/Documents/notes/target/`
3. Upload `notes-storage-service-key.json` from `/Users/subramanyatn/Documents/notes/`

#### **Method B: Use scp from Mac**
```bash
# On your Mac terminal
cd /Users/subramanyatn/Documents/notes

# Copy JAR
scp -i ~/.ssh/google_compute_engine target/notes-0.0.1-SNAPSHOT.jar \
  YOUR_USERNAME@VM_EXTERNAL_IP:~/

# Copy service key
scp -i ~/.ssh/google_compute_engine notes-storage-service-key.json \
  YOUR_USERNAME@VM_EXTERNAL_IP:~/
```

Replace:
- `YOUR_USERNAME`: Your Google username (usually your email before @)
- `VM_EXTERNAL_IP`: Your VM's external IP (from Step 1)

### Step 4: Run the Application on VM

In the SSH browser terminal:

```bash
# Check files are uploaded
ls -lh
# You should see: notes-0.0.1-SNAPSHOT.jar and notes-storage-service-key.json

# Run the app
java -jar notes-0.0.1-SNAPSHOT.jar
```

Watch for logs:
```
Started NotesApplication in X.XXX seconds
```

### Step 5: Authorize VM IP for Cloud SQL

1. Get VM External IP from: https://console.cloud.google.com/compute/instances?project=inbound-fulcrum-475913-n9
2. Go to Cloud SQL: https://console.cloud.google.com/sql/instances/notes-sql/connections/networking?project=inbound-fulcrum-475913-n9
3. Scroll to "Authorized networks"
4. Click "ADD NETWORK"
5. Enter:
   - Name: `notes-app-vm`
   - Network: `VM_EXTERNAL_IP/32` (e.g., `34.123.45.67/32`)
6. Click "DONE" → "SAVE"

### Step 6: Restart App
Back in SSH terminal:
- Press `Ctrl+C` to stop
- Run again: `java -jar notes-0.0.1-SNAPSHOT.jar`

### Step 7: Test from Your Mac

```bash
# Replace with your actual VM IP
export VM_IP=34.123.45.67

# Test
curl http://$VM_IP:8080/streams

# Login
TOKEN=$(curl -s -X POST http://$VM_IP:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@unifeed.com","password":"admin123"}' \
  | jq -r '.accessToken')

echo $TOKEN

# Create stream
curl -X POST http://$VM_IP:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "CSE"}'
```

### Step 8: Open in Browser
```
http://VM_EXTERNAL_IP:8080
```

Login:
- Email: `admin@unifeed.com`
- Password: `admin123`

---

## 🔄 To Run in Background

```bash
# Stop current app (Ctrl+C)

# Run in background
nohup java -jar notes-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# Check logs
tail -f app.log

# Check if running
ps aux | grep java
```

---

## 🛑 To Stop App

```bash
# Find process ID
ps aux | grep notes-0.0.1-SNAPSHOT.jar

# Kill it
kill -9 <PID>

# Or kill all java processes
pkill -f notes-0.0.1-SNAPSHOT.jar
```

---

## 📋 Quick Summary

1. Open SSH in browser (GCP Console)
2. Install Java 21 on VM
3. Upload JAR + service key via SSH window
4. Run: `java -jar notes-0.0.1-SNAPSHOT.jar`
5. Authorize VM IP in Cloud SQL
6. Access: `http://VM_IP:8080`

**No gcloud CLI needed!** Everything through browser! 🎯
