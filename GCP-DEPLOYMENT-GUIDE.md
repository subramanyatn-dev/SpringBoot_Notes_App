# Deploy Academic Notes App to Google Cloud VM

## 🎯 Overview

Deploy your Spring Boot application to a Google Cloud VM instance to make it publicly accessible.

## 📋 Prerequisites

### 1. Google Cloud Account
- ✅ GCP account with billing enabled
- ✅ Project created (you already have one with Cloud SQL and Storage)

### 2. Local Setup
- ✅ Google Cloud SDK installed (`gcloud` CLI)
- ✅ Your application running locally
- ✅ GCS bucket: `notes-pdfs`
- ✅ Service account key: `notes-storage-service-key.json`

### 3. Cloud SQL (Already Done ✅)
- MySQL instance running
- Database: `notes_db`
- Connection configured

---

## 🚀 Step-by-Step Deployment

### Step 1: Prepare Your Application

#### 1.1 Update `application.properties` for Production

Create `src/main/resources/application-prod.properties`:

```properties
# Server Configuration
server.port=8080

# Cloud SQL Connection (Use Cloud SQL Proxy or Public IP)
spring.datasource.url=jdbc:mysql://YOUR_CLOUD_SQL_IP:3306/notes_db?useSSL=false
spring.datasource.username=root
spring.datasource.password=YOUR_CLOUD_SQL_PASSWORD

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration
jwt.secret=your-production-secret-key-must-be-at-least-56-characters-long-for-security
app.jwt.ttl=1800000

# GCS Configuration
gcs.bucket.name=notes-pdfs
gcs.credentials.location=/opt/app/notes-storage-service-key.json

# Logging
logging.level.root=INFO
logging.level.com.app.notes=INFO
```

#### 1.2 Build Production JAR

```bash
# Clean and build
mvn clean package -DskipTests

# Verify JAR is created
ls -lh target/*.jar
```

---

### Step 2: Create Google Cloud VM

#### 2.1 Create VM Instance

```bash
# Set your project
gcloud config set project YOUR_PROJECT_ID

# Create VM instance
gcloud compute instances create notes-app-vm \
    --zone=us-central1-a \
    --machine-type=e2-medium \
    --image-family=ubuntu-2204-lts \
    --image-project=ubuntu-os-cloud \
    --boot-disk-size=20GB \
    --boot-disk-type=pd-standard \
    --tags=http-server,https-server \
    --scopes=cloud-platform
```

#### 2.2 Create Firewall Rule for Port 8080

```bash
# Allow HTTP traffic on port 8080
gcloud compute firewall-rules create allow-notes-app \
    --allow=tcp:8080 \
    --source-ranges=0.0.0.0/0 \
    --target-tags=http-server \
    --description="Allow access to Notes App on port 8080"
```

---

### Step 3: Set Up Cloud SQL Connection

#### Option A: Use Cloud SQL Proxy (Recommended)

```bash
# SSH into VM
gcloud compute ssh notes-app-vm --zone=us-central1-a

# Install Cloud SQL Proxy
wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O cloud_sql_proxy
chmod +x cloud_sql_proxy

# Create directory
sudo mkdir -p /opt/cloudsql

# Run proxy in background
./cloud_sql_proxy -dir=/opt/cloudsql -instances=YOUR_PROJECT:us-central1:YOUR_SQL_INSTANCE &
```

Update connection string:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/notes_db?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=YOUR_PROJECT:us-central1:YOUR_SQL_INSTANCE
```

#### Option B: Use Public IP (Simpler but less secure)

1. Enable Cloud SQL Public IP
2. Add VM's external IP to authorized networks
3. Use public IP in connection string

---

### Step 4: Install Java on VM

```bash
# SSH into VM
gcloud compute ssh notes-app-vm --zone=us-central1-a

# Update system
sudo apt update
sudo apt upgrade -y

# Install Java 21
sudo apt install openjdk-21-jdk -y

# Verify Java installation
java -version
```

---

### Step 5: Upload Application Files to VM

#### 5.1 Create Application Directory

```bash
# On VM
sudo mkdir -p /opt/app
sudo chown $USER:$USER /opt/app
```

#### 5.2 Upload Files from Local Machine

```bash
# From your local machine (not VM)
cd /Users/subramanyatn/Documents/notes

# Upload JAR file
gcloud compute scp target/notes-0.0.1-SNAPSHOT.jar \
    notes-app-vm:/opt/app/notes.jar \
    --zone=us-central1-a

# Upload GCS service account key
gcloud compute scp notes-storage-service-key.json \
    notes-app-vm:/opt/app/ \
    --zone=us-central1-a

# Upload production properties
gcloud compute scp src/main/resources/application-prod.properties \
    notes-app-vm:/opt/app/application.properties \
    --zone=us-central1-a
```

---

### Step 6: Run Application on VM

#### 6.1 Run Manually (For Testing)

```bash
# SSH into VM
gcloud compute ssh notes-app-vm --zone=us-central1-a

# Navigate to app directory
cd /opt/app

# Run the application
java -jar notes.jar --spring.config.location=application.properties
```

#### 6.2 Create Systemd Service (For Production)

```bash
# On VM - Create service file
sudo nano /etc/systemd/system/notes-app.service
```

Add this content:

```ini
[Unit]
Description=Academic Notes Management System
After=network.target

[Service]
Type=simple
User=YOUR_USERNAME
WorkingDirectory=/opt/app
ExecStart=/usr/bin/java -jar /opt/app/notes.jar --spring.config.location=/opt/app/application.properties
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start the service:

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable notes-app

# Start the service
sudo systemctl start notes-app

# Check status
sudo systemctl status notes-app

# View logs
sudo journalctl -u notes-app -f
```

---

### Step 7: Configure Domain (Optional)

#### 7.1 Get VM External IP

```bash
gcloud compute instances describe notes-app-vm \
    --zone=us-central1-a \
    --format='get(networkInterfaces[0].accessConfigs[0].natIP)'
```

#### 7.2 Set Up Domain

1. Go to your domain registrar (GoDaddy, Namecheap, etc.)
2. Add A record pointing to VM's external IP
3. Wait for DNS propagation (5-30 minutes)

#### 7.3 Set Up HTTPS with Let's Encrypt (Optional)

```bash
# Install Nginx as reverse proxy
sudo apt install nginx certbot python3-certbot-nginx -y

# Configure Nginx
sudo nano /etc/nginx/sites-available/notes-app
```

Add:
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

Enable and get SSL:
```bash
sudo ln -s /etc/nginx/sites-available/notes-app /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
sudo certbot --nginx -d your-domain.com
```

---

### Step 8: Access Your Application

#### Without Domain:
```
http://VM_EXTERNAL_IP:8080
```

#### With Domain:
```
https://your-domain.com
```

---

## 🔧 Configuration Checklist

### Before Deployment:

- [ ] Cloud SQL instance running and accessible
- [ ] GCS bucket created and service account has permissions
- [ ] Firewall rule created for port 8080
- [ ] Production `application.properties` configured
- [ ] JAR file built successfully
- [ ] Service account key file ready

### After Deployment:

- [ ] Application runs without errors
- [ ] Can connect to Cloud SQL
- [ ] Can upload files to GCS
- [ ] Login works with admin credentials
- [ ] CRUD operations work
- [ ] Systemd service enabled and running

---

## 🐛 Troubleshooting

### Application Won't Start

```bash
# Check logs
sudo journalctl -u notes-app -n 50

# Check if port 8080 is available
sudo netstat -tulpn | grep 8080

# Check Java version
java -version
```

### Can't Connect to Cloud SQL

```bash
# Test MySQL connection
mysql -h CLOUD_SQL_IP -u root -p

# Check Cloud SQL Proxy logs
ps aux | grep cloud_sql_proxy

# Verify service account permissions
gcloud projects get-iam-policy YOUR_PROJECT
```

### GCS Upload Fails

```bash
# Verify service account key permissions
cat /opt/app/notes-storage-service-key.json

# Test bucket access
gsutil ls gs://notes-pdfs/

# Check application logs for errors
sudo journalctl -u notes-app -n 100 | grep GCS
```

### Firewall Issues

```bash
# List firewall rules
gcloud compute firewall-rules list

# Test connection
curl http://VM_EXTERNAL_IP:8080

# Check VM tags
gcloud compute instances describe notes-app-vm --zone=us-central1-a
```

---

## 💰 Cost Estimation

**Monthly Costs (Approximate):**
- VM (e2-medium): ~$30-40/month
- Cloud SQL (small instance): ~$25-50/month
- Cloud Storage: ~$0.02/GB stored + data transfer
- **Total: ~$60-100/month**

**Cost Optimization:**
- Use smaller VM (e2-small) if traffic is low: ~$15/month
- Use preemptible VM for dev: ~$7/month (may restart)
- Stop VM when not needed: $0 when stopped

---

## 🔐 Security Best Practices

1. **Change Default Credentials**
   ```bash
   # Remove hardcoded demo users in production
   # Update AuthService.java to remove:
   # - admin@example.com/1234
   # - user@example.com/1234
   ```

2. **Use Environment Variables**
   ```bash
   # Don't hardcode sensitive data
   export DB_PASSWORD="secure_password"
   export JWT_SECRET="production_secret_key"
   ```

3. **Enable HTTPS**
   - Use Nginx + Let's Encrypt
   - Force HTTPS redirect

4. **Restrict IP Access** (Optional)
   ```bash
   # Allow only specific IPs
   gcloud compute firewall-rules update allow-notes-app \
       --source-ranges=YOUR_IP/32
   ```

5. **Regular Backups**
   ```bash
   # Automated Cloud SQL backups
   gcloud sql backups create --instance=YOUR_SQL_INSTANCE
   ```

---

## 📝 Quick Commands Reference

```bash
# Start application
sudo systemctl start notes-app

# Stop application
sudo systemctl stop notes-app

# Restart application
sudo systemctl restart notes-app

# View logs
sudo journalctl -u notes-app -f

# SSH into VM
gcloud compute ssh notes-app-vm --zone=us-central1-a

# Upload new JAR
gcloud compute scp target/notes-0.0.1-SNAPSHOT.jar \
    notes-app-vm:/opt/app/notes.jar --zone=us-central1-a

# Restart after upload
gcloud compute ssh notes-app-vm --zone=us-central1-a \
    --command="sudo systemctl restart notes-app"
```

---

## 🎉 Success Checklist

Once deployed, test these:

- [ ] Visit `http://VM_IP:8080` - See login page
- [ ] Register a new user account
- [ ] Login with admin credentials
- [ ] Create a stream
- [ ] Add semester
- [ ] Add subject  
- [ ] Upload a PDF note
- [ ] Verify PDF is in GCS bucket
- [ ] Download PDF from note link
- [ ] Test as USER (read-only)

---

Your app is now live on Google Cloud! 🚀
