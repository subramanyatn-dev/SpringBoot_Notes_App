# 🔐 Authorize Your IP Address for Cloud SQL Access

## ⚠️ Current Issue: Connection Timeout

The connection to Cloud SQL is timing out because your Mac's IP address is not authorized to connect.

## 🔧 Fix: Add Your IP to Authorized Networks

### Step 1: Get Your Public IP Address
Your current public IP will be displayed above (or run this command):
```bash
curl https://api.ipify.org
```

### Step 2: Add IP to Cloud SQL Authorized Networks

1. Go to Cloud SQL Console:
   https://console.cloud.google.com/sql/instances/notes-sql/connections/networking?project=inbound-fulcrum-475913-n9

2. Click on **"Connections"** tab (left sidebar)

3. Click on **"Networking"** section

4. Scroll to **"Authorized networks"** section

5. Click **"ADD NETWORK"**

6. Fill in:
   - **Name**: `My Mac` (or any name you want)
   - **Network**: `YOUR_PUBLIC_IP/32` (e.g., `203.0.113.45/32`)
   - The `/32` means only your specific IP address

7. Click **"DONE"** then **"SAVE"**

### Step 3: Wait 1-2 Minutes
It takes a moment for the firewall rules to apply.

### Step 4: Test Connection
```bash
mysql -h 34.10.105.237 -u notes_user -p
# Enter password: Notes@123
```

## 🌐 Alternative: Allow All IPs (Not Recommended for Production)

If you want to allow any IP (useful for testing but less secure):

1. Network: `0.0.0.0/0`
2. Name: `Allow All`

⚠️ **Warning**: This allows connections from anywhere. Only use for development!

## 🏠 For Dynamic IP Addresses

If your home IP changes frequently:
- Use Cloud SQL Proxy instead (more secure)
- Or add your IP range (e.g., `203.0.113.0/24`)
- Or use a VPN with static IP

## 🔐 Better Security: Use Cloud SQL Proxy

Instead of public IP, you can use Cloud SQL Proxy:

```bash
# Install Cloud SQL Proxy
curl -o cloud-sql-proxy https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.0/cloud-sql-proxy.darwin.amd64
chmod +x cloud-sql-proxy

# Run proxy (in separate terminal)
./cloud-sql-proxy inbound-fulcrum-475913-n9:us-central1:notes-sql

# Then connect via localhost
mysql -h 127.0.0.1 -u notes_user -p
```

And update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/notesdb
```

## 📝 Quick Command Reference

```bash
# Test connection
mysql -h 34.10.105.237 -u notes_user -pNotes@123 -e "SELECT 1"

# Show databases
mysql -h 34.10.105.237 -u notes_user -pNotes@123 -e "SHOW DATABASES"

# Check if notesdb exists
mysql -h 34.10.105.237 -u notes_user -pNotes@123 -D notesdb -e "SHOW TABLES"
```

## ✅ Once Connected

After authorizing your IP, your Spring Boot application will automatically:
1. Connect to Cloud SQL on startup
2. Create all necessary tables (users, streams, semesters, subjects, notes)
3. Start accepting API requests

Run:
```bash
mvn clean spring-boot:run
```

Look for successful connection logs:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```
