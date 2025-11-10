# Java Email Service - Deployment Guide

## 📋 Overview

This is a Java application that reads user data from Azure SQL Database and sends email notifications using Azure Communication Service. It receives requests from the API Service App.

**Technology:** Java 17 + Javalin (lightweight web framework)  
**Deployment Target:** Azure App Service  
**Build Tool:** Maven

---

## 🚀 Deployment Steps (Azure Portal UI Only)

### Prerequisites

1. A GitHub account with this repository
2. An Azure account (free tier works)
3. Azure SQL Database already created and populated
4. Azure Communication Service already created with email domain configured
5. API Service App already deployed (will call this service)

---

### Step 1: Create Azure App Service

1. **Go to Azure Portal**

   - Open: https://portal.azure.com
   - Sign in to your account

2. **Create New Resource**

   - Click "+ Create a resource"
   - Search for "App Service"
   - Click "Create" → "Web App"

3. **Configure Basic Settings**

   - **Subscription:** Select your subscription
   - **Resource Group:** Create new or use existing (e.g., `user-data-system-rg`)
   - **Name:** `java-email-service` (or your preferred name - must be globally unique)
   - **Publish:** Code
   - **Runtime stack:** Java 17
   - **Java web server stack:** Java SE (Embedded Web Server)
   - **Operating System:** Linux
   - **Region:** Choose closest to you (e.g., East US, West Europe)

4. **Configure App Service Plan**

   - **Linux Plan:** Create new or use existing
   - **Pricing Plan:** Free F1 or Basic B1 (Free works for testing)

5. **Review + Create**

   - Click "Review + create"
   - Click "Create"
   - Wait 2-3 minutes for deployment

6. **Get Your Service URL**
   - After deployment completes, click "Go to resource"
   - Find your URL (e.g., `https://java-email-service.azurewebsites.net`)
   - **Save this URL** - you'll need it for API Service App configuration

---

### Step 2: Configure Environment Variables

This service requires several environment variables to connect to Azure services:

1. **Go to Your App Service**

   - In Azure Portal, open your App Service resource
   - Click "Configuration" in the left menu
   - Click "Application settings" tab

2. **Add Environment Variables** - Click "+ New application setting" for each:

   **a) SQL_CONNECTION_STRING**

   - **Name:** `SQL_CONNECTION_STRING`
   - **Value:** Your SQL Database connection string
   - Format: `jdbc:sqlserver://YOUR_SERVER.database.windows.net:1433;database=YOUR_DATABASE;user=YOUR_USERNAME;password=YOUR_PASSWORD;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;`
   - **How to get it:**
     - Go to your SQL Database in Azure Portal
     - Click "Connection strings" in left menu
     - Copy "JDBC" connection string
     - Replace `{your_username}` and `{your_password}` with actual values
   - Click "OK"

   **b) COMMUNICATION_SERVICE_CONNECTION_STRING**

   - **Name:** `COMMUNICATION_SERVICE_CONNECTION_STRING`
   - **Value:** Your Azure Communication Service connection string
   - **How to get it:**
     - Go to your Communication Service in Azure Portal
     - Click "Keys" in left menu
     - Copy "Primary connection string"
   - Click "OK"

   **c) SENDER_EMAIL_ADDRESS**

   - **Name:** `SENDER_EMAIL_ADDRESS`
   - **Value:** Your verified sender email address
   - Format: `DoNotReply@your-domain.azurecomm.net`
   - **How to get it:**
     - Go to your Communication Service in Azure Portal
     - Click "Email" → "Domains" in left menu
     - Find your verified domain
     - Use format: `DoNotReply@yourdomainname.azurecomm.net`
   - Click "OK"

3. **Save Configuration**
   - Click "Save" at the top
   - Click "Continue" to confirm restart
   - Wait 1-2 minutes for app to restart

---

### Step 3: Configure GitHub Deployment

1. **Get Publish Profile from Azure**

   - In Azure Portal, go to your App Service
   - Click "Download publish profile" at the top
   - Save the `.PublishSettings` file

2. **Add Secret to GitHub**

   - Go to your GitHub repository
   - Click "Settings" tab
   - Click "Secrets and variables" → "Actions"
   - Click "New repository secret"
   - **Name:** `AZURE_WEBAPP_PUBLISH_PROFILE_EMAIL_SERVICE`
   - **Value:** Open the downloaded `.PublishSettings` file in notepad, copy ALL content
   - Click "Add secret"

3. **Update Workflow File (if needed)**
   - File location: `.github/workflows/azure-deploy.yml`
   - Change `AZURE_WEBAPP_NAME` if you used different name:
   ```yaml
   env:
     AZURE_WEBAPP_NAME: your-app-name # Change this if different
   ```

---

### Step 4: Deploy the Application

1. **Trigger Deployment**

   - Option 1: Push code to GitHub main branch
   - Option 2: Go to GitHub → Actions → Select workflow → "Run workflow"

2. **Monitor Deployment**

   - Go to GitHub repository → "Actions" tab
   - Watch the workflow run
   - Wait for green checkmark ✅ (takes 3-5 minutes)
   - If red ❌, click on it to see error logs

3. **Verify Deployment**
   - Open your App Service URL: `https://your-app-name.azurewebsites.net/api/health`
   - Should see: `{"success":true,"message":"Email service is healthy"}`
   - If you see error, check App Service logs (see Troubleshooting section)

---

### Step 5: Configure API Service App

Now you need to tell the API Service App where this email service is:

1. **Go to API Service App in Azure Portal**

   - Open your API Service App resource
   - Click "Configuration" → "Application settings"

2. **Add/Update Email Service URL**
   - Find or create setting: `EMAIL_SERVICE_URL`
   - **Value:** `https://your-java-email-service.azurewebsites.net`
   - Click "Save"
   - Wait for restart

---

### Step 6: Test End-to-End

1. **Test Health Endpoint**

   - Open: `https://your-java-email-service.azurewebsites.net/api/health`
   - Should return: `{"success":true,"message":"Email service is healthy"}`

2. **Test via Email Notification App**

   - Open your Email Notification App
   - Enter a receiver email address
   - Click "Send Email Notification"
   - Should receive success message
   - Check receiver's inbox/spam for email

3. **Verify Email Content**
   - Email should have subject: "User Data Report"
   - Email should contain HTML table with all database records
   - Table should show: ID, Name, Age, Created At, Email

---

## 🔧 Configuration Details

### Environment Variables Required

| Variable                                  | Description                                   | Example                           |
| ----------------------------------------- | --------------------------------------------- | --------------------------------- |
| `SQL_CONNECTION_STRING`                   | Azure SQL Database JDBC connection string     | `jdbc:sqlserver://...`            |
| `COMMUNICATION_SERVICE_CONNECTION_STRING` | Azure Communication Service connection string | `endpoint=https://...`            |
| `SENDER_EMAIL_ADDRESS`                    | Verified sender email address                 | `DoNotReply@domain.azurecomm.net` |

### API Endpoints

#### 1. Health Check

```
GET /api/health
```

**Response:**

```json
{
  "success": true,
  "message": "Email service is healthy"
}
```

#### 2. Send Email

```
POST /api/email/send
Content-Type: application/json

{
  "receiverEmail": "user@example.com"
}
```

**Response (Success):**

```json
{
  "success": true,
  "message": "Email sent successfully to user@example.com"
}
```

**Response (Error):**

```json
{
  "success": false,
  "message": "Error description"
}
```

---

## 🗂️ Database Table Expected

The service expects this table structure:

```sql
CREATE TABLE UserData (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100) NOT NULL,
    Age INT NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETUTCDATE(),
    Email NVARCHAR(255) NULL
);
```

---

## 🐛 Troubleshooting

### Issue: App Won't Start

**Symptoms:** App Service shows "Application Error"

**Solutions:**

1. Check App Service logs:
   - Go to App Service → "Log stream" in left menu
   - Look for error messages
2. Verify environment variables are set correctly
3. Verify Java 17 is selected as runtime
4. Check that JAR file was built successfully in GitHub Actions

### Issue: Database Connection Fails

**Symptoms:** Error message about SQL connection

**Solutions:**

1. Verify `SQL_CONNECTION_STRING` is correct
2. Check SQL Database firewall rules:
   - Go to SQL Server → "Networking"
   - Add rule: "Allow Azure services and resources to access this server"
   - Add your IP if testing locally
3. Verify database name, username, password are correct
4. Test connection string format is JDBC format (not .NET format)

### Issue: Email Not Sending

**Symptoms:** API returns error "Failed to send email"

**Solutions:**

1. Verify `COMMUNICATION_SERVICE_CONNECTION_STRING` is correct
2. Verify `SENDER_EMAIL_ADDRESS` matches your verified domain
3. Check Communication Service email domain is verified
4. Check receiver email is valid
5. Look at App Service logs for detailed error
6. Verify Communication Service has email sending enabled

### Issue: GitHub Actions Fails

**Symptoms:** Red ❌ in GitHub Actions

**Solutions:**

1. Check secret `AZURE_WEBAPP_PUBLISH_PROFILE_EMAIL_SERVICE` is set
2. Verify publish profile is valid (re-download if needed)
3. Check Maven build succeeds (look at build logs)
4. Verify `pom.xml` has correct Java version (17)
5. Check app name in workflow matches Azure App Service name

### Issue: CORS Errors

**Symptoms:** API Service can't call Email Service

**Solutions:**

1. Service already has CORS enabled for all origins
2. If needed, configure CORS in Azure App Service:
   - Go to App Service → "CORS"
   - Add API Service URL to allowed origins
   - Click "Save"

---

## 📊 Monitoring & Logs

### View Application Logs

1. Go to App Service in Azure Portal
2. Click "Log stream" in left menu
3. See real-time logs from application
4. Look for:
   - "Java Email Service started on port: 8080"
   - "Processing email request for: ..."
   - "Email sent successfully to: ..."

### View Deployment History

1. Go to App Service
2. Click "Deployment Center"
3. See deployment history and status

### Monitor Requests

1. Go to App Service
2. Click "Metrics" in left menu
3. Add metric: "Requests"
4. Add metric: "Response Time"
5. Add metric: "Http Server Errors"

---

## 🔄 Making Updates

### Update Code

1. Edit Java files in `java-email-service` folder
2. Test locally if possible (`mvn clean package && java -jar target/*.jar`)
3. Commit and push to GitHub
4. GitHub Actions automatically builds and deploys
5. Wait 3-5 minutes for deployment
6. Test endpoints again

### Update Dependencies

1. Edit `pom.xml`
2. Update version numbers
3. Push to GitHub
4. Auto-deploys via GitHub Actions

### Update Environment Variables

1. Go to App Service → Configuration
2. Modify application settings
3. Click "Save"
4. App restarts automatically

---

## 📧 Email Template

The service creates a beautiful HTML email with:

- **Header:** "User Data Report"
- **Table:** All database records with columns:
  - ID
  - Name
  - Age
  - Created At (formatted timestamp)
  - Email (or "N/A" if empty)
- **Styling:** Light blue theme matching other apps
- **Footer:** "Powered by Azure Communication Service"

---

## 🔐 Security Notes

- ✅ All secrets in environment variables
- ✅ Database uses encrypted connection (TLS)
- ✅ Communication Service uses secure connection
- ✅ CORS configured (adjust in production)
- ✅ SQL injection protection (JDBC handles escaping)
- ✅ HTML escaping in email template
- ⚠️ Email validation is basic (improve if needed)

---

## 🎯 Success Checklist

Before considering deployment complete:

- [ ] App Service created in Azure
- [ ] Java 17 runtime selected
- [ ] All 3 environment variables configured
- [ ] GitHub secret configured
- [ ] GitHub Actions workflow runs successfully
- [ ] Health endpoint returns success
- [ ] API Service App has EMAIL_SERVICE_URL configured
- [ ] Can send test email via Email Notification App
- [ ] Email received with correct table data
- [ ] No errors in App Service logs

---

## 💡 Tips

1. **Test Database First:** Make sure database has data before testing email
2. **Check Logs:** Log stream is your friend for debugging
3. **Verify Domain:** Email domain must be verified in Communication Service
4. **Port Configuration:** App automatically uses PORT environment variable from Azure
5. **Build Time:** First build takes longer (~3-5 minutes)
6. **Restart App:** After changing environment variables, app auto-restarts

---

## 📚 Dependencies Used

- **Javalin 5.6.3** - Lightweight web framework
- **Azure Communication Email 1.0.13** - Sending emails
- **MS SQL Server JDBC 12.4.2** - Database connection
- **Gson 2.10.1** - JSON parsing
- **SLF4J 2.0.9** - Logging

---

**Need Help?** Check `SYSTEM_ARCHITECTURE.md` for complete system overview and how all apps connect together.
