# Quick Setup Instructions

## 1. Reset the Database

```bash
./reset-auth-db.sh
```

## 2. Restart the Auth Service

```bash
cd auth-service
mvn spring-boot:run
```

Or if using the start-all script:

```bash
./start-all.sh
```

## 3. Access the Application

Open http://localhost:3000 and login with:

**Admin Account:**
- Username: `admin`
- Password: `admin123`

**Receptionist Accounts:**
- Username: `sohaib` / Password: `root1312`
- Username: `othmane` / Password: `root1312`

## What You Can Do as Admin

Once logged in as admin, you'll see two additional tabs:

### 1. Gestion Docteurs
- Add new doctors
- Edit doctor information (name, specialty, email, phone)
- Delete doctors

### 2. Gestion Réceptionnistes
- Create new receptionist accounts
- Edit receptionist info (username, email, password)
- Enable/disable receptionist accounts
- Delete receptionists
- **Note:** You cannot create ADMIN accounts from the UI

## Important Notes

- ✅ Registration is **disabled** from the web UI (more secure)
- ✅ Only **one admin account** exists (cannot be created from UI)
- ✅ Admin can create **RECEPTIONIST accounts only**
- ✅ Admin role is **locked** and cannot be changed from UI
- ✅ All appointments can be **modified and deleted**

## First Time Setup Checklist

- [ ] Run `./reset-auth-db.sh`
- [ ] Restart auth-service
- [ ] Login as admin
- [ ] Change admin password (through Gestion Réceptionnistes tab)
- [ ] Create receptionist accounts as needed
- [ ] Add doctors to the system

## Need Help?

See `ADMIN_SECURITY_GUIDE.md` for detailed security information and troubleshooting.
