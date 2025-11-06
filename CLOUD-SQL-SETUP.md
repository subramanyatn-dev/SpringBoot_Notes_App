# ☁️ Cloud SQL Configuration

## 🔐 Connection Details

- **Instance Name**: `notes-sql`
- **Connection Name**: `inbound-fulcrum-475913-n9:us-central1:notes-sql`
- **Public IP**: `34.10.105.237`
- **Port**: `3306`
- **Database**: `notesdb`
- **Username**: `notes_user`
- **Password**: `Notes@123`
- **Region**: `us-central1`

## 📝 Configuration Files

### `application.properties` (Active - Cloud SQL)
Currently configured to use **Cloud SQL** with public IP connection.

### `application-local.properties` (Backup - Local MySQL)
Backup of your local MySQL configuration. To use local MySQL instead:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 🔄 Switch Between Configurations

### Use Cloud SQL (Default)
```bash
mvn clean spring-boot:run
```

### Use Local MySQL
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 🔒 Security Notes

1. **SSL Enabled**: Cloud SQL requires SSL connections (configured in application.properties)
2. **Authorized Networks**: Currently allows 1 authorized network
3. **Password**: Store securely, don't commit to Git (already in .gitignore)
4. **Service Account**: `p731293845459-pslfil@gcp-sa-cloud-sql.iam.gserviceaccount.com`

## 📊 Cloud SQL Features Enabled

- ✅ Public IP Connectivity
- ✅ App Engine Authorization
- ✅ SSL/TLS Encryption
- ✅ Automatic Backups (Daily)
- ❌ High Availability (Consider enabling for production)
- ❌ Private IP Connectivity (Optional for better security)

## 🚀 First-Time Setup

When you first connect to Cloud SQL, the application will automatically:
1. Create all tables (streams, semesters, subjects, notes, users)
2. Set up relationships and indexes
3. Enable UTF-8 character encoding

**Note**: Your local data will NOT be automatically migrated. See migration section below.

## 🔄 Migrate Local Data to Cloud SQL

If you have existing data in local MySQL that you want to migrate:

### Option 1: Manual Migration
```bash
# Export from local MySQL
mysqldump -u notes_user -p notesdb > local_backup.sql

# Import to Cloud SQL
mysql -h 34.10.105.237 -u notes_user -p notesdb < local_backup.sql
```

### Option 2: Using gcloud
```bash
# Create bucket for SQL dump
gsutil mb gs://notes-sql-backup

# Export local data
mysqldump -u notes_user -p notesdb > local_backup.sql

# Upload to GCS
gsutil cp local_backup.sql gs://notes-sql-backup/

# Import to Cloud SQL via GCP Console
# Go to: Cloud SQL → notes-sql → Import
```

## 🧪 Test Connection

### Using MySQL CLI
```bash
mysql -h 34.10.105.237 -u notes_user -p
# Enter password: Notes@123

USE notesdb;
SHOW TABLES;
```

### Using Application
```bash
mvn clean spring-boot:run

# Check logs for:
# "HikariPool-1 - Start completed"
# "Hibernate: create table if not exists..."
```

## 📈 Monitoring

- **Cloud SQL Dashboard**: https://console.cloud.google.com/sql/instances/notes-sql?project=inbound-fulcrum-475913-n9
- **Operations Log**: View backups, updates, and errors
- **MySQL Error Logs**: Available in GCP Console

## 💰 Cost Estimate

Current Configuration:
- Machine Type: db-f1-micro (0.6 GB RAM)
- Storage: 10 GB SSD
- Estimated Cost: ~$7-10/month

## ⚡ Performance Optimization

Current settings in `application.properties`:
- Maximum Pool Size: 5 connections
- Minimum Idle: 2 connections
- Connection Timeout: 30 seconds

For production, consider:
- Upgrading to db-n1-standard-1 (3.75 GB RAM)
- Enabling High Availability
- Setting up Read Replicas

## 🔧 Troubleshooting

### Connection Refused
1. Check if your IP is in authorized networks
2. Verify firewall rules allow port 3306
3. Ensure Cloud SQL instance is running

### SSL Errors
- SSL is required by Cloud SQL
- Configuration already includes SSL settings
- No additional certificates needed for public IP connection

### Slow Queries
- Check Cloud SQL metrics in GCP Console
- Review connection pool settings
- Consider upgrading instance size

## 📚 Documentation

- [Cloud SQL for MySQL](https://cloud.google.com/sql/docs/mysql)
- [Connecting from External Applications](https://cloud.google.com/sql/docs/mysql/connect-external-app)
- [Best Practices](https://cloud.google.com/sql/docs/mysql/best-practices)
