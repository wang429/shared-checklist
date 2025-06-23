# Shared Checklist Setup Guide

This application has two different authentication modes depending on the environment:

## Development Mode (Default)
- **No authentication required** - all users have access
- Uses **Alice** as the default user for all operations
- Uses **H2 in-memory database** with sample data
- Perfect for development and testing

## Production Mode
- **Google OAuth2 authentication** required
- Users sign in with their Google accounts
- Uses **PostgreSQL database**
- Secure multi-user environment

---

## Running in Development Mode

### Prerequisites
- Java 21
- Node.js and Yarn

### Steps
1. **Backend**: Run with dev profile (default)
   ```bash
   cd backend
   ./gradlew bootRun
   ```

2. **Frontend**: Start the development server
   ```bash
   cd frontend
   yarn install
   yarn dev
   ```

3. **Access**: Open http://localhost:5173
   - No login required
   - Automatically logged in as "Alice"
   - All sample data available

---

## Running in Production Mode

### Prerequisites
- Java 21
- PostgreSQL database
- Google Cloud Console project with OAuth2 credentials

### Setup Google OAuth2

1. **Create Google Cloud Project**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing

2. **Enable Google+ API**:
   - Go to "APIs & Services" > "Library"
   - Search for "Google+ API" and enable it

3. **Create OAuth2 Credentials**:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "OAuth 2.0 Client IDs"
   - Application type: "Web application"
   - Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
   - Note down the Client ID and Client Secret

### Environment Variables

Set these environment variables for production:

```bash
# Database
export DATABASE_URL=jdbc:postgresql://localhost:5432/checklistdb
export DATABASE_USERNAME=your_db_user
export DATABASE_PASSWORD=your_db_password

# Google OAuth2
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export OAUTH_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
```

### Steps

1. **Database Setup**:
   ```sql
   CREATE DATABASE checklistdb;
   ```

2. **Backend**: Run with prod profile
   ```bash
   cd backend
   ./gradlew bootRun --args='--spring.profiles.active=prod'
   ```

3. **Frontend**: Build and serve
   ```bash
   cd frontend
   yarn build
   # Serve the built files through your web server
   ```

4. **Access**: Open http://localhost:8080
   - You'll be redirected to Google login
   - After authentication, you'll have access to the checklist
   - New users are automatically created on first login

---

## Key Differences

| Feature | Development | Production |
|---------|-------------|------------|
| Authentication | None (auto-login as Alice) | Google OAuth2 |
| Database | H2 in-memory | PostgreSQL |
| User Creation | Pre-seeded sample users | Auto-created from Google profile |
| Security | Disabled | Full OAuth2 security |
| Data Persistence | Lost on restart | Persistent |

---

## Configuration Files

- `application.yml` - Common configuration, defaults to dev profile
- `application-dev.yml` - Development-specific settings
- `application-prod.yml` - Production-specific settings with OAuth2

---

## Switching Profiles

You can change the active profile by:

1. **Application properties**:
   ```yaml
   spring:
     profiles:
       active: prod
   ```

2. **Command line**:
   ```bash
   --spring.profiles.active=prod
   ```

3. **Environment variable**:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   ```

---

## Troubleshooting

### Development Mode Issues
- **Port conflicts**: Backend runs on 8080, frontend on 5173
- **Database issues**: H2 console available at http://localhost:8080/h2-console

### Production Mode Issues
- **OAuth redirect mismatch**: Ensure redirect URI matches Google Console settings
- **Database connection**: Verify PostgreSQL is running and accessible
- **Environment variables**: Double-check all required variables are set

### Common Issues
- **CORS errors**: Make sure frontend is properly proxying API calls
- **Build failures**: Run `./gradlew clean build` to refresh dependencies 