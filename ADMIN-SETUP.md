# Admin User Creation Guide

## Current Admin Accounts

The system has 2 pre-configured demo admin accounts (hardcoded):
- Email: `admin@example.com` | Password: `1234`
- Email: `user@example.com` | Password: `1234` (USER role)

## Security Policy

⚠️ **IMPORTANT**: Regular users CANNOT register as ADMIN through the registration page.
- All registrations via the UI create USER accounts only (read-only access)
- ADMIN accounts must be created manually via database

## How to Create Additional Admin Accounts

### Method 1: Using MySQL Command Line

```bash
# Login to MySQL
mysql -u root -p

# Switch to notes_db
USE notes_db;

# Create admin user
INSERT INTO users (name, email, password, role) 
VALUES ('Admin Name', 'admin2@example.com', 'securepassword', 'ADMIN');

# Verify
SELECT * FROM users WHERE role = 'ADMIN';

# Exit
EXIT;
```

### Method 2: Using the Script

```bash
# Run the script
./create-admin.sh

# Follow the prompts to enter:
# - Full Name
# - Email
# - Password
```

### Method 3: Using curl (Direct Database Insert Not Possible via API)

❌ The `/auth/register` endpoint will REJECT any attempt to create ADMIN accounts.

## Recommended Admin Setup

1. **Development**: Use the 2 pre-configured demo accounts
2. **Production**: 
   - Create 2 admin accounts via MySQL before deploying
   - Remove or change the hardcoded demo account passwords in `AuthService.java`
   - Use strong passwords
   - Store passwords securely (consider password hashing in future)

## User Roles

| Role | Permissions |
|------|------------|
| ADMIN | Full access: Create, Read, Update, Delete all content |
| USER | Read-only: View streams, semesters, subjects, and notes |

## Example: Create Production Admins

```sql
-- Create your main admin
INSERT INTO users (name, email, password, role) 
VALUES ('Your Name', 'youremail@company.com', 'strongpassword123', 'ADMIN');

-- Create backup admin
INSERT INTO users (name, email, password, role) 
VALUES ('Backup Admin', 'admin@company.com', 'anotherpassword456', 'ADMIN');
```

## Security Best Practices

1. ✅ Limit admin accounts to 2-3 trusted users
2. ✅ Use strong passwords (8+ characters, mixed case, numbers, symbols)
3. ✅ Never share admin credentials
4. ✅ Change default demo passwords in production
5. ✅ Consider implementing password hashing (BCrypt) in future
6. ✅ Monitor admin activity logs

## Troubleshooting

**Q: I accidentally tried to register as ADMIN and got an error**
A: This is expected! ADMIN registration is blocked. Contact your system administrator.

**Q: How do I change an existing user to ADMIN?**
A: Use MySQL:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'user@example.com';
```

**Q: Can I delete the demo admin accounts?**
A: Yes, but only after creating your own admin accounts. Don't lock yourself out!
