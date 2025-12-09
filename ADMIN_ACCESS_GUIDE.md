# Admin Access Guide

## Default Users After Setup

After running the setup, you will have these users:

| Username | Password  | Role   | Purpose |
|----------|-----------|--------|---------|
| admin    | admin123  | ADMIN  | Full management access |
| sohaib   | root1312  | DOCTOR | Regular doctor user |
| othmane  | root1312  | DOCTOR | Regular doctor user |

## Option 1: Reset Database (Recommended for Development)

If you already have a database with users, follow these steps:

```bash
# 1. Stop the auth-service if it's running
# Press Ctrl+C in the terminal where it's running

# 2. Reset the auth database
./reset-auth-db.sh

# 3. Restart the auth-service
cd auth-service
mvn spring-boot:run
```

The admin user will be created automatically when the auth-service starts.

## Option 2: Use Registration to Create Admin

If you don't want to reset the database:

1. Start all services
2. Open the frontend (http://localhost:3000)
3. Click on "S'inscrire" (Register)
4. Fill in the form:
   - **Username:** your_admin_name
   - **Password:** your_password
   - **Email:** your_email@hospital.ma
   - **Role:** Select "ADMIN"
5. Click "S'inscrire"
6. You'll be automatically logged in as admin

## Option 3: Use Existing User and Update via API

If you want to promote an existing user to admin:

```bash
# Login as an existing user first to get a token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "sohaib",
    "password": "root1312"
  }'

# Copy the token from the response, then update the user role
# Replace YOUR_TOKEN_HERE with the actual token
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "role": "ADMIN"
  }'
```

## Accessing Management Features

Once logged in as admin:

1. **Login** with admin credentials
2. You'll see two additional tabs:
   - **Gestion Docteurs** - Add, edit, delete doctors
   - **Gestion Utilisateurs** - Add, edit, delete users

## Managing Users as Admin

As an admin, you can:
- ✅ Create new users with any role (DOCTOR or ADMIN)
- ✅ Edit user information (username, email, role)
- ✅ Change user passwords
- ✅ Enable/disable user accounts
- ✅ Delete users
- ✅ Add, edit, delete doctors
- ✅ Modify appointments

## Security Notes

⚠️ **IMPORTANT:** Change the default admin password after first login!

You can do this through:
1. The "Gestion Utilisateurs" tab (as admin)
2. Or by updating your own user record

## Troubleshooting

### "Database already exists" error
Run `./reset-auth-db.sh` to reset the database.

### Admin tabs not showing
Make sure:
- You're logged in as a user with role "ADMIN"
- Check the browser console for errors
- Verify your token is valid

### Can't login as admin
- Check if auth-service is running on port 8081
- Verify the database was reset and auth-service restarted
- Try using the registration form to create a new admin
