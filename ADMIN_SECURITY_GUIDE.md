# Admin Access & Security Guide

## Security Model

This application follows a secure model where:
- **Only ONE admin account** exists (hardcoded in DataInitializer)
- **Admin accounts CANNOT be created from the web UI**
- Admin can only create RECEPTIONIST accounts
- Public registration is completely disabled

## Default Users

After running the database reset, you will have:

| Username | Password  | Role          | Can Be Deleted? |
|----------|-----------|---------------|-----------------|
| admin    | admin123  | ADMIN         | No (only one)   |
| sohaib   | root1312  | RECEPTIONIST  | Yes             |
| othmane  | root1312  | RECEPTIONIST  | Yes             |

## How to Access Admin Account

### Step 1: Reset the Database

```bash
./reset-auth-db.sh
```

### Step 2: Restart Auth Service

```bash
cd auth-service
mvn spring-boot:run
```

### Step 3: Login

Go to http://localhost:3000 and login with:
- **Username:** `admin`
- **Password:** `admin123`

**⚠️ IMPORTANT:** Change the default admin password immediately after first login!

## Admin Capabilities

As admin, you can:
- ✅ **Add, edit, and delete doctors** (Gestion Docteurs tab)
- ✅ **Create RECEPTIONIST accounts** (Gestion Réceptionnistes tab)
- ✅ **Edit receptionist information** (username, email, password)
- ✅ **Enable/disable receptionist accounts**
- ✅ **Delete receptionist accounts**
- ✅ **Manage appointments** (modify, delete)

## What Admin CANNOT Do

- ❌ **Create additional ADMIN accounts** (only one admin exists)
- ❌ **Delete the admin account** (protected)
- ❌ **Change admin role** (role is locked to ADMIN)

## Receptionist Capabilities

Receptionists can:
- View list of doctors
- Create appointments
- View and manage appointments
- Cannot access admin management panels

## Changing Admin Password

### Option 1: Through the UI (Recommended)

1. Login as admin
2. Go to "Gestion Réceptionnistes" tab
3. Find the admin user in the table
4. Click "Modifier" (Edit)
5. Enter a new password
6. Click "Enregistrer" (Save)

### Option 2: Through Code

Edit `auth-service/src/main/java/com/healthcare/auth/DataInitializer.java`:

```java
admin.setPassword(passwordEncoder.encode("your-new-secure-password"));
```

Then reset the database and restart the service.

## Adding More Admin Users (If Needed)

If you need multiple admin users, edit the DataInitializer.java file:

```java
// Add this after the existing admin user
User admin2 = new User();
admin2.setUsername("admin2");
admin2.setPassword(passwordEncoder.encode("secure-password"));
admin2.setEmail("admin2@hospital.ma");
admin2.setRole("ADMIN");
admin2.setEnabled(true);
repository.save(admin2);
```

Then reset the database and restart the service.

## Security Best Practices

1. **Change default passwords immediately**
2. **Use strong passwords** (minimum 12 characters, mix of letters, numbers, symbols)
3. **Don't share admin credentials**
4. **Regularly review user accounts** and disable unused ones
5. **Monitor login attempts** (check logs for suspicious activity)

## Troubleshooting

### Can't login as admin
- Verify the database was reset: `./reset-auth-db.sh`
- Check auth-service is running on port 8081
- Verify credentials: username=admin, password=admin123

### "Registration disabled" error
- This is expected - registration is disabled for security
- Only admin can create users through the admin panel

### Admin tabs not showing
- Verify you're logged in as a user with role "ADMIN"
- Check browser console for errors
- Refresh the page

## Database Schema

The User table has these fields:
- `id` - Primary key
- `username` - Unique, min 3 characters
- `password` - Hashed with BCrypt
- `email` - Unique, must be valid email
- `role` - ADMIN or RECEPTIONIST
- `enabled` - Boolean, controls account status
