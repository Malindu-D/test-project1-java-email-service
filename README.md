# ☕ Java Email Service

A Java application that reads user data from Azure SQL Database and sends email notifications with a formatted HTML table using Azure Communication Service.

## 🎯 Purpose

This service receives requests from the API Service App to send email notifications. It retrieves all user data from the database, formats it as an HTML table, and sends it to the specified receiver email address.

## 🎨 Features

- ✅ REST API endpoints (health check + send email)
- ✅ Reads all records from Azure SQL Database
- ✅ Creates beautiful HTML email with data table
- ✅ Sends emails via Azure Communication Service
- ✅ Lightweight and fast (Javalin framework)
- ✅ Environment variable configuration
- ✅ Automatic deployment via GitHub Actions

## 🛠️ Technology Stack

- **Language:** Java 17
- **Framework:** Javalin 5.6.3 (lightweight web framework)
- **Database:** Azure SQL Database (JDBC)
- **Email:** Azure Communication Service
- **Build Tool:** Maven
- **Deployment:** Azure App Service

## 📁 Project Structure

```
java-email-service/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── userdata/
│                   └── emailservice/
│                       ├── App.java                      # Main application
│                       ├── models/
│                       │   ├── UserData.java            # User data model
│                       │   ├── EmailRequest.java        # Request model
│                       │   └── ApiResponse.java         # Response model
│                       └── services/
│                           ├── DatabaseService.java     # SQL Database operations
│                           ├── EmailService.java        # Azure Communication Service
│                           └── EmailTemplateBuilder.java # HTML email generator
├── .github/
│   └── workflows/
│       └── azure-deploy.yml         # GitHub Actions workflow
├── pom.xml                          # Maven configuration
├── DEPLOYMENT.md                    # Deployment guide
├── README.md                        # This file
└── SYSTEM_ARCHITECTURE.md           # Complete system overview
```

## 🚀 How It Works

### Request Flow:

```
Email Notification App → API Service App → Java Email Service
    ↓
Java Email Service queries Azure SQL Database
    ↓
Creates HTML email with table of all user data
    ↓
Sends email via Azure Communication Service
    ↓
Returns success/failure response
```

## 🔗 API Endpoints

### 1. Health Check

```http
GET /api/health
```

**Response:**

```json
{
  "success": true,
  "message": "Email service is healthy"
}
```

### 2. Send Email

```http
POST /api/email/send
Content-Type: application/json

{
  "receiverEmail": "user@example.com"
}
```

**Success Response:**

```json
{
  "success": true,
  "message": "Email sent successfully to user@example.com"
}
```

**Error Response:**

```json
{
  "success": false,
  "message": "Error description"
}
```

## ⚙️ Configuration

### Environment Variables:

- `SQL_CONNECTION_STRING` - JDBC connection string for Azure SQL Database
- `COMMUNICATION_SERVICE_CONNECTION_STRING` - Azure Communication Service connection
- `SENDER_EMAIL_ADDRESS` - Verified sender email address
- `PORT` - Server port (automatically set by Azure App Service)

### Example Configuration:

```bash
SQL_CONNECTION_STRING=jdbc:sqlserver://yourserver.database.windows.net:1433;database=yourdb;user=username;password=pass;encrypt=true;
COMMUNICATION_SERVICE_CONNECTION_STRING=endpoint=https://yourservice.communication.azure.com/;accesskey=yourkey
SENDER_EMAIL_ADDRESS=DoNotReply@yourdomain.azurecomm.net
PORT=8080
```

## 📧 Email Template

The service generates a professional HTML email with:

- **Subject:** "User Data Report"
- **Header:** Blue styled header with title
- **Table:** All user records with columns:
  - ID
  - Name
  - Age
  - Created At (formatted timestamp)
  - Email (or "N/A" if null)
- **Styling:** Light blue theme (#4A90E2) matching static web apps
- **Footer:** Branding and powered-by message

## 🏃 Running Locally

### Prerequisites:

- Java 17 installed
- Maven installed
- Azure SQL Database accessible
- Azure Communication Service configured

### Build:

```bash
cd java-email-service
mvn clean package
```

### Run:

```bash
# Set environment variables
export SQL_CONNECTION_STRING="jdbc:sqlserver://..."
export COMMUNICATION_SERVICE_CONNECTION_STRING="endpoint=https://..."
export SENDER_EMAIL_ADDRESS="DoNotReply@domain.azurecomm.net"

# Run the application
java -jar target/java-email-service-1.0.0.jar
```

### Test:

```bash
# Health check
curl http://localhost:8080/api/health

# Send email
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -d '{"receiverEmail":"test@example.com"}'
```

## 🚀 Deployment

### Via GitHub Actions (Automatic):

1. Push code to GitHub main branch
2. GitHub Actions automatically builds with Maven
3. Deploys to Azure App Service
4. See `DEPLOYMENT.md` for detailed setup instructions

### Manual Build:

```bash
mvn clean package
```

The executable JAR will be in `target/java-email-service-1.0.0.jar`

## 🐛 Troubleshooting

### Common Issues:

**Database Connection Failed:**

- Verify `SQL_CONNECTION_STRING` is correct
- Check database firewall allows Azure services
- Verify database credentials

**Email Not Sending:**

- Verify `COMMUNICATION_SERVICE_CONNECTION_STRING` is valid
- Check `SENDER_EMAIL_ADDRESS` matches verified domain
- Ensure Communication Service has email enabled

**App Won't Start:**

- Check Java 17 is installed
- Verify all environment variables are set
- Look at application logs for errors

**CORS Errors:**

- Service has CORS enabled for all origins
- Can configure specific origins if needed

## 📊 Database Schema

Expected table structure:

```sql
CREATE TABLE UserData (
    Id INT PRIMARY KEY IDENTITY(1,1),
    Name NVARCHAR(100) NOT NULL,
    Age INT NOT NULL,
    CreatedAt DATETIME2 DEFAULT GETUTCDATE(),
    Email NVARCHAR(255) NULL
);
```

## 🔐 Security

- ✅ All sensitive data in environment variables
- ✅ Encrypted database connection (TLS)
- ✅ Secure Communication Service connection
- ✅ SQL injection protection (parameterized queries)
- ✅ HTML escaping in email templates
- ✅ Email validation
- ✅ CORS configured

## 📦 Dependencies

| Dependency                | Version | Purpose                   |
| ------------------------- | ------- | ------------------------- |
| Javalin                   | 5.6.3   | Lightweight web framework |
| Azure Communication Email | 1.0.13  | Sending emails            |
| MS SQL Server JDBC        | 12.4.2  | Database connectivity     |
| Gson                      | 2.10.1  | JSON serialization        |
| SLF4J Simple              | 2.0.9   | Logging                   |

## 🔄 Updates

To update the application:

1. Edit Java source files
2. Test locally with Maven
3. Commit and push to GitHub
4. GitHub Actions auto-deploys to Azure
5. Changes live in 3-5 minutes

## 📚 Documentation

- **DEPLOYMENT.md** - Complete Azure Portal deployment guide
- **SYSTEM_ARCHITECTURE.md** - Full system overview with all 5 applications
- **pom.xml** - Maven project configuration

## 💡 Tips

- **Log Monitoring:** Use Azure App Service Log Stream for real-time logs
- **Error Handling:** All errors are logged with stack traces
- **Email Testing:** Test with your own email first
- **Database Testing:** Ensure database has data before testing
- **Performance:** Service is lightweight and responds quickly
- **Scaling:** Can scale up App Service plan if needed

## 🎯 Integration

This service is part of a larger system:

- **Called by:** API Service App (receives email requests)
- **Reads from:** Azure SQL Database (user data)
- **Sends via:** Azure Communication Service (emails)
- **Triggered by:** Email Notification App (via API Service)

See `SYSTEM_ARCHITECTURE.md` for complete data flow and architecture.

---

**Part of the User Data Collection System**  
Version 1.0.0
