# 🚀 ProdPulse.AI - Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?style=for-the-badge&logo=spring)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Groq](https://img.shields.io/badge/Groq-LLaMA%203.3-purple?style=for-the-badge&logo=ai)

**AI-Powered Production Log Analyzer**

Instantly diagnose production errors with intelligent AI analysis powered by Groq's LLaMA 3.3 model.

[Features](#-features) • [Tech Stack](#-tech-stack) • [Installation](#-installation) • [API Documentation](#-api-documentation) • [Configuration](#-configuration)

</div>

---

## 📋 Overview

ProdPulse.AI is an intelligent backend service that analyzes production error logs and provides actionable insights using AI. Built with Spring Boot and powered by Groq's LLaMA 3.3-70B model, it helps developers quickly diagnose and fix production issues.

### ✨ Key Features

- 🤖 **AI-Powered Analysis**: Leverages Groq's LLaMA 3.3-70B for intelligent log diagnosis
- ⚡ **Fast Response**: Optimized for quick turnaround on error analysis
- 🔒 **Rate Limiting**: Built-in IP-based rate limiting (10 requests per 24 hours)
- 💾 **History Tracking**: Stores all analyses in MySQL database
- 🛡️ **Error Handling**: Comprehensive exception handling with fallback responses
- 🌐 **CORS Enabled**: Ready for frontend integration
- 📊 **Severity Detection**: Automatically categorizes errors (Critical/Warning/Info)

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming Language |
| **Spring Boot** | 3.5.9 | Backend Framework |
| **Spring AI** | 1.1.2 | AI Integration |
| **Groq API** | Latest | LLM Provider (LLaMA 3.3-70B) |
| **MySQL** | 8.0+ | Database |
| **Hibernate** | 6.6.39 | ORM |
| **Lombok** | 1.18.42 | Boilerplate Reduction |
| **Maven** | Latest | Build Tool |

---

## 🚀 Installation

### Prerequisites

- ☕ Java 21 or higher
- 🗄️ MySQL 8.0 or higher
- 🔑 Groq API Key ([Get one here](https://console.groq.com))
- 📦 Maven

### Step 1: Clone the Repository

```bash
git clone https://github.com/yourusername/prodpulse-backend.git
cd prodpulse-backend
```

### Step 2: Configure Database

Create a MySQL database:

```sql
CREATE DATABASE prodpulse_db;
```

### Step 3: Configure Application

Create `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/prodpulse_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Groq AI Configuration
spring.ai.openai.api-key=gsk_your_groq_api_key_here
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
spring.ai.openai.chat.options.temperature=0.3
spring.ai.openai.chat.options.max-tokens=2000

# CORS Configuration (add your frontend URL)
cors.allowed-origins=http://localhost:3000,https://your-frontend.netlify.app

# Rate Limiting
app.rate-limit.max-requests=10
app.rate-limit.window-hours=24
```

### Step 4: Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The server will start at `http://localhost:8080`

---

## 📡 API Documentation

### 1. Analyze Logs

Analyze production error logs with AI.

**Endpoint:** `POST /api/analyze`

**Request Body:**
```json
{
  "logs": "Error: connect ECONNREFUSED 127.0.0.1:3306\n   at TCPConnectWrap.afterConnect\n   MySQL connection failed"
}
```

**Response:** `200 OK`
```json
{
  "severity": "critical",
  "title": "Error: connect ECONNREFUSED 127.0.0.1:3306",
  "content": "<div class=\"diagnosis\">...</div>",
  "timestamp": "2025-12-31T16:56:08",
  "analysisId": 33
}
```

**Error Responses:**
- `400 Bad Request` - Invalid input
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

---

### 2. Health Check

Check if the API is running.

**Endpoint:** `GET /api/health`

**Response:** `200 OK`
```json
{
  "status": "UP",
  "service": "ProdPulse.AI Backend",
  "timestamp": 1735651568000
}
```

---

### 3. Rate Limit Status

Check remaining requests for current IP.

**Endpoint:** `GET /api/rate-limit-status`

**Response:** `200 OK`
```json
{
  "remainingRequests": 8,
  "ipAddress": "127.0.0.1"
}
```

---

### 4. API Info

Get API information and available endpoints.

**Endpoint:** `GET /api/`

**Response:** `200 OK`
```json
{
  "name": "ProdPulse.AI API",
  "version": "1.0.0",
  "description": "AI-powered production log analyzer",
  "endpoints": {
    "POST /api/analyze": "Analyze production error logs",
    "GET /api/health": "Health check",
    "GET /api/rate-limit-status": "Check remaining requests"
  }
}
```

---

## ⚙️ Configuration

### Rate Limiting

Adjust rate limiting in `application.properties`:

```properties
# Allow 20 requests per 12 hours
app.rate-limit.max-requests=20
app.rate-limit.window-hours=12
```

### AI Model Settings

Customize AI behavior:

```properties
# Use different Groq model
spring.ai.openai.chat.options.model=llama-3.1-8b-instant

# Adjust creativity (0.0 - 1.0)
spring.ai.openai.chat.options.temperature=0.5

# Increase response length
spring.ai.openai.chat.options.max-tokens=3000
```

### CORS Configuration

Configure allowed origins for frontend:

```properties
# Multiple origins separated by comma
cors.allowed-origins=http://localhost:3000,https://app.example.com
```

---

## 🏗️ Project Structure

```
prodpulse-backend/
├── src/main/java/com/prodpulse/prodpulse_backend/
│   ├── config/
│   │   ├── CorsConfig.java           # CORS configuration
│   │   └── GroqConfig.java           # Groq AI setup
│   ├── controller/
│   │   └── LogAnalysisController.java # REST endpoints
│   ├── service/
│   │   ├── AIService.java            # AI integration
│   │   └── LogAnalysisService.java   # Business logic
│   ├── model/
│   │   ├── dto/                      # Data Transfer Objects
│   │   └── entity/                   # Database entities
│   ├── repository/
│   │   └── AnalysisHistoryRepository.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── RateLimitException.java
│   │   └── InvalidLogException.java
│   └── ProdPulseBackendApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## 🔍 How It Works

1. **Log Submission**: User sends error logs via POST `/api/analyze`
2. **Validation**: System validates input and checks rate limits
3. **AI Analysis**: Groq's LLaMA 3.3-70B analyzes the error
4. **Response Generation**: AI generates structured diagnosis with:
   - 🔍 What Happened (root cause)
   - 🔧 How to Fix (step-by-step solutions)
   - 💡 Prevention Tips (best practices)
5. **Storage**: Analysis saved to MySQL for history tracking
6. **Response**: Formatted HTML response returned to client

---

## 🐛 Troubleshooting

### Database Connection Issues

```bash
# Check MySQL is running
sudo service mysql status

# Verify credentials in application.properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### Groq API Errors

```bash
# Verify API key is correct
spring.ai.openai.api-key=gsk_...

# Check base URL (no /v1 at the end!)
spring.ai.openai.base-url=https://api.groq.com/openai
```

### Port Already in Use

```bash
# Change port in application.properties
server.port=8081
```

---

## 🚢 Deployment

### Deploy to Railway

1. Create a new project on [Railway](https://railway.app)
2. Add MySQL database service
3. Deploy from GitHub repository
4. Set environment variables:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `SPRING_AI_OPENAI_API_KEY`
   - `CORS_ALLOWED_ORIGINS`

### Deploy with Docker

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build
docker build -t prodpulse-backend .

# Run
docker run -p 8080:8080 prodpulse-backend
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 👨‍💻 Author

**Your Name**

- GitHub: [@yourusername](https://github.com/Sumeet-Y1)
- LinkedIn: [Your Name](https://www.linkedin.com/in/sumeet-backenddev/)
- Website: [yourwebsite.com](sumeetdev.netlify.app/)
- Email: (sumeety202@gmail.com)

---

## 🙏 Acknowledgments

- [Groq](https://groq.com) for the amazing LLM API
- [Spring Boot](https://spring.io/projects/spring-boot) for the robust framework
- [Spring AI](https://spring.io/projects/spring-ai) for AI integration
- All contributors and supporters

---

<div align="center">

**⭐ Star this repo if you find it helpful!**

Made with ❤️ and ☕ by developers, for developers

</div>
