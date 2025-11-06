# 🚀 Deploy Spring Boot Notes App to GCP VM - Step by Step

## 📋 Prerequisites

Before starting, make sure you have:
- ✅ Cloud SQL instance running (`34.10.105.237`)
- ✅ GCS bucket created (`notes-pdfs`)
- ✅ Service account key file (`notes-storage-service-key.json`)
- ✅ GCP Project: `inbound-fulcrum-475913-n9`
- ✅ Local app working with Cloud SQL

---

## 🎯 Step 1: Create a VM Instance

### 1.1 Go to GCP Console
Open: https://console.cloud.google.com/compute/instances?project=inbound-fulcrum-475913-n9

### 1.2 Click "CREATE INSTANCE"

### 1.3 Configure VM:
```
Name: notes-app-vm
Region: us-central1 (same as your Cloud SQL)
Zone: us-central1-a

Machine Configuration:
- Series: E2
- Machine type: e2-micro (2 vCPU, 1 GB memory) - FREE TIER
  OR
- Machine type: e2-medium (2 vCPU, 4 GB memory) - $24/month (Recommended)

Boot disk:
- Operating System: Ubuntu
- Version: Ubuntu 22.04 LTS
- Boot disk type: Balanced persistent disk
- Size: 10 GB

Firewall:
☑️ Allow HTTP traffic
☑️ Allow HTTPS traffic
```

### 1.4 Click "CREATE"

Wait 1-2 minutes for VM to start. Note the **External IP** (e.g., `34.123.45.67`)

---

## 🔥 Step 2: Configure Firewall for Port 8080

### 2.1 Go to Firewall Rules
https://console.cloud.google.com/networking/firewalls/list?project=inbound-fulcrum-475913-n9

### 2.2 Click "CREATE FIREWALL RULE"

### 2.3 Configure:
```
Name: allow-spring-boot-8080
Description: Allow traffic on port 8080 for Spring Boot app
Direction: Ingress
Action on match: Allow
Targets: All instances in the network

Source filter: IP ranges
Source IP ranges: 0.0.0.0/0

Protocols and ports:
☑️ Specified protocols and ports
tcp: 8080
```

### 2.4 Click "CREATE"

---

## 🔧 Step 3: Build Your Application

On your **local Mac**:

```bash
cd /Users/subramanyatn/Documents/notes

# Clean and build JAR file
mvn clean package -DskipTests

# Check JAR was created
ls -lh target/*.jar
```

You should see: `target/notes-0.0.1-SNAPSHOT.jar` (around 50-70 MB)

---

## 📤 Step 4: Upload Files to VM

### 4.1 Upload JAR file:
```bash
gcloud compute scp target/notes-0.0.1-SNAPSHOT.jar notes-app-vm:~/ \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9
```

### 4.2 Upload service account key:
```bash
gcloud compute scp notes-storage-service-key.json notes-app-vm:~/ \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9
```

### 4.3 Upload application.properties:
```bash
gcloud compute scp src/main/resources/application.properties notes-app-vm:~/ \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9
```

---

## 🖥️ Step 5: SSH into VM and Install Java

### 5.1 SSH into VM:
```bash
gcloud compute ssh notes-app-vm \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9
```

You're now inside the VM! (prompt will change)

### 5.2 Update system:
```bash
sudo apt update
```

### 5.3 Install Java 21:
```bash
sudo apt install -y openjdk-21-jdk
```

### 5.4 Verify Java installation:
```bash
java -version
```

Should show: `openjdk version "21.x.x"`

### 5.5 Check uploaded files:
```bash
ls -lh
```

You should see:
- `notes-0.0.1-SNAPSHOT.jar`
- `notes-storage-service-key.json`
- `application.properties`

---

## 🚀 Step 6: Run the Application

### 6.1 Test run (foreground):
```bash
java -jar notes-0.0.1-SNAPSHOT.jar
```

Watch for:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
Started NotesApplication in X seconds
```

**Test from your Mac:**
```bash
curl http://VM_EXTERNAL_IP:8080/streams
```

Press `Ctrl+C` to stop the app.

### 6.2 Run in background with nohup:
```bash
nohup java -jar notes-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### 6.3 Check if running:
```bash
ps aux | grep java
```

### 6.4 View logs:
```bash
tail -f app.log
```

Press `Ctrl+C` to stop viewing logs.

---

## 🔄 Step 7: Create Systemd Service (Auto-start on Reboot)

### 7.1 Create service file:
```bash
sudo nano /etc/systemd/system/notes-app.service
```

### 7.2 Paste this content:
```ini
[Unit]
Description=Spring Boot Notes Application
After=network.target

[Service]
Type=simple
User=YOUR_USERNAME
WorkingDirectory=/home/YOUR_USERNAME
ExecStart=/usr/bin/java -jar /home/YOUR_USERNAME/notes-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

**Replace `YOUR_USERNAME`** with your actual username (usually same as your email before @)

To find your username: `whoami`

### 7.3 Save file:
- Press `Ctrl+X`
- Press `Y`
- Press `Enter`

### 7.4 Reload systemd:
```bash
sudo systemctl daemon-reload
```

### 7.5 Enable service (auto-start on boot):
```bash
sudo systemctl enable notes-app
```

### 7.6 Start service:
```bash
sudo systemctl start notes-app
```

### 7.7 Check status:
```bash
sudo systemctl status notes-app
```

Should show: `Active: active (running)`

### 7.8 View logs:
```bash
sudo journalctl -u notes-app -f
```

---

## 🌐 Step 8: Access Your Application

### 8.1 Get VM External IP:
```bash
gcloud compute instances describe notes-app-vm \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9 \
  --format='get(networkInterfaces[0].accessConfigs[0].natIP)'
```

### 8.2 Test API from your Mac:

```bash
# Replace with your actual VM IP
export VM_IP=34.123.45.67

# Test health
curl http://$VM_IP:8080/streams

# Login as admin
TOKEN=$(curl -s -X POST http://$VM_IP:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@unifeed.com","password":"admin123"}' \
  | jq -r '.accessToken')

echo "Token: $TOKEN"

# Create stream
curl -X POST http://$VM_IP:8080/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "CSE"}' | jq
```

### 8.3 Access from browser:
```
http://VM_EXTERNAL_IP:8080
```

---

## 🔒 Step 9: Authorize VM IP for Cloud SQL

Your VM needs to connect to Cloud SQL!

### 9.1 Get VM External IP:
Already got it in Step 8.1

### 9.2 Add to Cloud SQL Authorized Networks:
1. Go to: https://console.cloud.google.com/sql/instances/notes-sql/connections/networking?project=inbound-fulcrum-475913-n9
2. Scroll to "Authorized networks"
3. Click "ADD NETWORK"
4. Enter:
   - Name: `notes-app-vm`
   - Network: `VM_EXTERNAL_IP/32` (e.g., `34.123.45.67/32`)
5. Click "DONE" → "SAVE"
6. Wait 1 minute

### 9.3 Restart app on VM:
```bash
sudo systemctl restart notes-app
```

---

## 🎨 Step 10: Access Frontend

Your frontend HTML is served at:
```
http://VM_EXTERNAL_IP:8080/
```

Open in browser and you'll see the login page!

**Test credentials:**
- Email: `admin@unifeed.com`
- Password: `admin123`

---

## 🛠️ Management Commands

### Start app:
```bash
sudo systemctl start notes-app
```

### Stop app:
```bash
sudo systemctl stop notes-app
```

### Restart app:
```bash
sudo systemctl restart notes-app
```

### View logs:
```bash
sudo journalctl -u notes-app -f
```

### Check status:
```bash
sudo systemctl status notes-app
```

### SSH into VM:
```bash
gcloud compute ssh notes-app-vm \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9
```

### Upload new JAR (after code changes):
```bash
# Build locally
mvn clean package -DskipTests

# Upload to VM
gcloud compute scp target/notes-0.0.1-SNAPSHOT.jar notes-app-vm:~/ \
  --zone=us-central1-a \
  --project=inbound-fulcrum-475913-n9

# SSH and restart
gcloud compute ssh notes-app-vm --zone=us-central1-a --project=inbound-fulcrum-475913-n9
sudo systemctl restart notes-app
```

---

## 🔍 Troubleshooting

### App won't start:
```bash
# Check logs
sudo journalctl -u notes-app -n 100 --no-pager

# Check if port 8080 is in use
sudo netstat -tuln | grep 8080

# Kill existing process
sudo pkill -f notes-0.0.1-SNAPSHOT.jar
sudo systemctl restart notes-app
```

### Can't connect to Cloud SQL:
```bash
# Test from VM
mysql -h 34.10.105.237 -u notes_user -p
# Password: Notes@123

# Check if VM IP is authorized in Cloud SQL
```

### Firewall issues:
```bash
# Test if port 8080 is open
curl http://VM_IP:8080/streams

# Check firewall rules
gcloud compute firewall-rules list --project=inbound-fulcrum-475913-n9 | grep 8080
```

### High memory usage:
```bash
# Run with limited memory
sudo nano /etc/systemd/system/notes-app.service

# Change ExecStart line to:
ExecStart=/usr/bin/java -Xmx512m -Xms256m -jar /home/YOUR_USERNAME/notes-0.0.1-SNAPSHOT.jar

# Reload and restart
sudo systemctl daemon-reload
sudo systemctl restart notes-app
```

---

## 💰 Cost Estimate

**VM (e2-micro):**
- Free Tier: 1 instance per month (limited to certain regions)
- Cost if not free: ~$7/month

**VM (e2-medium - Recommended):**
- Cost: ~$24/month

**Cloud SQL (db-f1-micro):**
- Cost: ~$7-10/month

**GCS (Storage):**
- Cost: ~$0.02/GB/month + operations

**Total Estimated Cost:**
- With e2-micro: ~$14-17/month (or ~$7-10 if VM is free tier)
- With e2-medium: ~$31-34/month

---

## 🎯 Quick Summary

1. ✅ Create VM (e2-micro or e2-medium)
2. ✅ Create firewall rule for port 8080
3. ✅ Build JAR locally (`mvn clean package -DskipTests`)
4. ✅ Upload JAR, service key, and properties to VM
5. ✅ SSH into VM and install Java 21
6. ✅ Run app with systemd service
7. ✅ Authorize VM IP in Cloud SQL
8. ✅ Access at `http://VM_IP:8080`

---

## 🌟 Next Steps (Optional)

- [ ] Set up custom domain (e.g., `notes.yourdomain.com`)
- [ ] Configure Nginx as reverse proxy
- [ ] Enable HTTPS with Let's Encrypt
- [ ] Set up monitoring with GCP Operations
- [ ] Configure auto-scaling
- [ ] Set up CI/CD pipeline

---

## 📞 Support

If you encounter issues:
1. Check logs: `sudo journalctl -u notes-app -f`
2. Verify Cloud SQL connection
3. Check firewall rules
4. Ensure VM has enough memory

---

**Ready to deploy!** 🚀

Start with **Step 1** and work through each step carefully. Let me know when you're ready or if you need help with any step!
