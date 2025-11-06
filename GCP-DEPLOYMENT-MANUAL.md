# Manual Deployment Guide - GCP VM

## Prerequisites Checklist

Before starting, make sure you have:
- ✅ Google Cloud account with billing enabled
- ✅ Cloud SQL MySQL instance already created
- ✅ GCS bucket already created (notes-pdfs)
- ✅ GCS service account key JSON file
- ✅ gcloud CLI installed on your local machine

## Part 1: Prepare Your Application

### Step 1: Update application.properties for Production

Create `src/main/resources/application-prod.properties`:

```properties
# Server Configuration
server.port=8080

# Cloud SQL Configuration (UPDATE THESE!)
spring.datasource.url=jdbc:mysql://YOUR_CLOUD_SQL_IP:3306/notes_db
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Configuration (UPDATE THIS!)
jwt.secret=change-this-to-a-very-long-random-secret-key-at-least-64-characters-long
jwt.expiration=1800000

# GCS Configuration (UPDATE THESE!)
gcs.bucket.name=notes-pdfs
gcs.credentials.location=/home/your_username/notes-storage-service-key.json

# Logging
logging.level.root=INFO
logging.level.com.app.notes=INFO
```

### Step 2: Build the JAR file

```bash
# Clean and build
mvn clean package -DskipTests

# The JAR will be in target/notes-0.0.1-SNAPSHOT.jar
```

## Part 2: Create and Configure GCP VM

### Step 3: Create a VM Instance

1. Go to GCP Console → Compute Engine → VM Instances
2. Click "CREATE INSTANCE"
3. Configure:
   - **Name**: `notes-app-vm`
   - **Region**: Choose same region as your Cloud SQL (e.g., us-central1)
   - **Zone**: Any zone in that region (e.g., us-central1-a)
   - **Machine type**: 
     - For testing: `e2-micro` (2 vCPU, 1 GB RAM) - Free tier eligible
     - For production: `e2-medium` (2 vCPU, 4 GB RAM)
   - **Boot disk**: 
     - Click "CHANGE"
     - Operating system: Ubuntu
     - Version: Ubuntu 22.04 LTS
     - Boot disk type: Standard persistent disk
     - Size: 20 GB
   - **Firewall**: 
     - ✅ Allow HTTP traffic
     - ✅ Allow HTTPS traffic
4. Click "CREATE"

### Step 4: Configure Firewall Rules

1. Go to VPC Network → Firewall
2. Click "CREATE FIREWALL RULE"
3. Configure:
   - **Name**: `allow-http-8080`
   - **Direction**: Ingress
   - **Targets**: All instances in the network
   - **Source IP ranges**: `0.0.0.0/0`
   - **Protocols and ports**: 
     - ✅ Specified protocols and ports
     - tcp: `8080`
4. Click "CREATE"

## Part 3: Connect VM to Cloud SQL

### Step 5: Get Cloud SQL Connection Info

1. Go to Cloud SQL → Instances
2. Click your instance name
3. Note down:
   - **Public IP address**: (e.g., 34.72.123.45)
   - **Private IP address**: (if using private IP)
   - **Connection name**: (e.g., project-id:region:instance-name)

### Step 6: Authorize VM IP in Cloud SQL

1. In Cloud SQL instance → Connections
2. Go to "Networking" tab
3. Click "ADD NETWORK"
4. Get your VM's external IP:
   - Go to Compute Engine → VM instances
   - Copy the "External IP"
5. Add this IP to authorized networks:
   - **Name**: `vm-instance`
   - **Network**: `VM_EXTERNAL_IP/32`
6. Click "DONE" then "SAVE"

## Part 4: Install Software on VM

### Step 7: SSH into VM

```bash
# From your local machine
gcloud compute ssh notes-app-vm --zone=us-central1-a
```

Or click "SSH" button in GCP Console.

### Step 8: Install Java 21

```bash
# Update system
sudo apt update
sudo apt upgrade -y

# Install Java 21
sudo apt install -y openjdk-21-jdk

# Verify installation
java -version
```

### Step 9: Install MySQL Client (Optional, for testing)

```bash
sudo apt install -y mysql-client

# Test connection to Cloud SQL
mysql -h YOUR_CLOUD_SQL_IP -u your_db_user -p
# Enter password and test
# If successful, type: exit;
```

## Part 5: Upload Files to VM

### Step 10: Upload JAR and Service Account Key

From your local machine (NEW TERMINAL, not in SSH):

```bash
# Upload JAR file
gcloud compute scp target/notes-0.0.1-SNAPSHOT.jar notes-app-vm:~/ --zone=us-central1-a

# Upload GCS service account key
gcloud compute scp notes-storage-service-key.json notes-app-vm:~/ --zone=us-central1-a

# Upload production properties
gcloud compute scp src/main/resources/application-prod.properties notes-app-vm:~/ --zone=us-central1-a
```

## Part 6: Run the Application

### Step 11: Start the Application (Back in VM SSH)

```bash
# Method 1: Simple run (for testing)
java -jar notes-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Method 2: Run in background with nohup
nohup java -jar notes-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &

# Method 3: Run with custom memory settings
nohup java -Xms512m -Xmx1024m -jar notes-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### Step 12: Check if Application is Running

```bash
# Check process
ps aux | grep java

# Check logs
tail -f app.log

# Check if port 8080 is listening
sudo netstat -tlnp | grep 8080

# Test locally on VM
curl http://localhost:8080
```

## Part 7: Access Your Application

### Step 13: Get VM External IP

```bash
# On VM
curl ifconfig.me

# Or from GCP Console: Compute Engine → VM instances → External IP
```

### Step 14: Access the Application

```
http://YOUR_VM_EXTERNAL_IP:8080
```

Example: `http://34.72.123.45:8080`

## Part 8: Set Up as System Service (Optional but Recommended)

### Step 15: Create Systemd Service

On VM, create service file:

```bash
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
WorkingDirectory=/home/YOUR_USERNAME
ExecStart=/usr/bin/java -jar /home/YOUR_USERNAME/notes-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Replace `YOUR_USERNAME` with your actual username (run `whoami` to find it).

### Step 16: Enable and Start Service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable notes-app

# Start service
sudo systemctl start notes-app

# Check status
sudo systemctl status notes-app

# View logs
sudo journalctl -u notes-app -f
```

### Step 17: Manage the Service

```bash
# Stop application
sudo systemctl stop notes-app

# Restart application
sudo systemctl restart notes-app

# Check if running
sudo systemctl is-active notes-app

# View recent logs
sudo journalctl -u notes-app -n 100
```

## Part 9: Set Up Domain (Optional)

### Step 18: Reserve Static IP

1. Go to VPC Network → IP addresses
2. Click "RESERVE EXTERNAL STATIC ADDRESS"
3. Configure:
   - **Name**: `notes-app-ip`
   - **Network Service Tier**: Premium
   - **IP version**: IPv4
   - **Type**: Regional
   - **Region**: Same as VM
   - **Attached to**: Select your VM
4. Click "RESERVE"

### Step 19: Configure Domain DNS

In your domain registrar (e.g., GoDaddy, Namecheap):

1. Add A Record:
   - **Type**: A
   - **Name**: `notes` (or `@` for root domain)
   - **Value**: Your static IP address
   - **TTL**: 3600

Wait 10-60 minutes for DNS propagation.

## Part 10: Set Up HTTPS with Let's Encrypt (Optional)

### Step 20: Install Nginx

```bash
sudo apt install -y nginx
```

### Step 21: Configure Nginx as Reverse Proxy

```bash
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
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable the site:

```bash
sudo ln -s /etc/nginx/sites-available/notes-app /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### Step 22: Install SSL Certificate

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal is set up automatically
# Test renewal
sudo certbot renew --dry-run
```

## Troubleshooting

### Application won't start

```bash
# Check Java version
java -version

# Check if port is already in use
sudo netstat -tlnp | grep 8080

# Check application logs
tail -f app.log
# or
sudo journalctl -u notes-app -n 50
```

### Can't connect to Cloud SQL

```bash
# Test connection
mysql -h CLOUD_SQL_IP -u username -p

# Check if VM IP is authorized in Cloud SQL
# Go to Cloud SQL → Connections → Authorized networks

# Check if Cloud SQL is public or private IP
```

### Can't access from browser

```bash
# Check if application is running
curl http://localhost:8080

# Check firewall rules (GCP Console → VPC Network → Firewall)

# Check if VM external IP is correct
curl ifconfig.me
```

### Application crashes

```bash
# Check memory usage
free -h

# Increase memory allocation
java -Xms512m -Xmx1024m -jar notes-0.0.1-SNAPSHOT.jar

# Check disk space
df -h
```

## Cost Estimation

**e2-micro (Free Tier)**:
- 1 vCPU, 1 GB RAM
- ~$7/month (free 1 instance per month in US regions)

**e2-medium (Recommended)**:
- 2 vCPU, 4 GB RAM
- ~$30/month

**Cloud SQL**:
- db-f1-micro: ~$10/month
- db-n1-standard-1: ~$50/month

**Cloud Storage**:
- Standard: $0.02 per GB/month
- Data transfer: First 1 GB free/month

## Quick Command Reference

```bash
# Start application manually
java -jar notes-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# Start in background
nohup java -jar notes-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > app.log 2>&1 &

# Check running Java processes
ps aux | grep java

# Kill application
pkill -f notes-0.0.1-SNAPSHOT.jar

# View logs
tail -f app.log

# System service commands
sudo systemctl start notes-app
sudo systemctl stop notes-app
sudo systemctl restart notes-app
sudo systemctl status notes-app

# View service logs
sudo journalctl -u notes-app -f
```

## Summary Checklist

Before going live:
- [ ] Cloud SQL instance created and accessible
- [ ] GCS bucket created with proper permissions
- [ ] VM instance created with correct machine type
- [ ] Firewall rule for port 8080 created
- [ ] Java 21 installed on VM
- [ ] JAR file uploaded to VM
- [ ] GCS service account key uploaded to VM
- [ ] application-prod.properties configured with correct values
- [ ] Application starts successfully
- [ ] Can access application via VM external IP
- [ ] Database connection working
- [ ] File uploads to GCS working
- [ ] (Optional) Domain configured
- [ ] (Optional) HTTPS with Let's Encrypt
- [ ] (Optional) Application running as systemd service

Your application will be accessible at:
- HTTP: `http://YOUR_VM_IP:8080`
- With domain: `http://your-domain.com`
- With HTTPS: `https://your-domain.com`
