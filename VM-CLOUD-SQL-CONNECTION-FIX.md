# ⚠️ URGENT: VM Can't Connect to Cloud SQL

## Problem
```
Communications link failure
Connect timed out
```

This means your **VM IP is NOT authorized** in Cloud SQL!

## 🔧 Quick Fix:

### Step 1: Get VM External IP
Go to: https://console.cloud.google.com/compute/instances?project=inbound-fulcrum-475913-n9

Find `notes-app-vm` and copy the **External IP** (e.g., `34.123.45.67`)

### Step 2: Authorize VM IP in Cloud SQL
1. Go to: https://console.cloud.google.com/sql/instances/notes-sql/connections/networking?project=inbound-fulcrum-475913-n9

2. Scroll down to **"Authorized networks"** section

3. Click **"ADD NETWORK"**

4. Fill in:
   - **Name**: `notes-app-vm`
   - **Network**: `PASTE_VM_EXTERNAL_IP/32`
   
   Example: If VM IP is `34.123.45.67`, enter:
   ```
   34.123.45.67/32
   ```

5. Click **"DONE"**

6. Click **"SAVE"** (at the bottom of the page)

7. **WAIT 1-2 MINUTES** for firewall rules to apply

### Step 3: Restart App on VM

In your VM SSH terminal:

```bash
# Stop current app
pkill -f notes-0.0.1-SNAPSHOT.jar

# Wait 5 seconds
sleep 5

# Start again
java -jar notes-0.0.1-SNAPSHOT.jar
```

You should now see:
```
HikariPool-1 - Start completed
Started NotesApplication in X.XXX seconds
```

---

## ✅ Verification

Once authorized, you'll see these logs:
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@...
HikariPool-1 - Start completed.
Hibernate: create table if not exists...
Started NotesApplication in 15.234 seconds
```

---

## 📝 Why This Happened

Cloud SQL has a firewall that blocks all connections by default. You need to explicitly authorize each IP address that should connect to it.

**Authorized IPs so far:**
- ✅ Your Mac: `49.37.241.173/32`
- ❌ Your VM: Need to add!

---

## 🎯 Action Required

1. Get VM External IP
2. Add to Cloud SQL authorized networks: `VM_IP/32`
3. Save and wait 1-2 minutes
4. Restart app on VM

**Do this now and your app will start successfully!** 🚀
