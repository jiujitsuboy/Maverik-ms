# MovieService Microservice 📽️

A production-grade Spring Boot 3 microservice to manage Movies, search external APIs, and export data.

---

## 🛠 Tech Stack
- Java 21
- Spring Boot 3.2
- Spring Data JPA
- Spring WebFlux (WebClient)
- Spring Security (API Key auth)
- Resilience4j (Retry + CircuitBreaker)
- SQL Server 2022
- Docker & Docker Compose

---

## 🚀 How to Run Locally

### 1. Build the Application
```bash
./mvnw clean package
```

### 2. Run with Docker Compose
```bash
docker-compose up --build
```

This will start:
- SQL Server on port 1433
- MovieService app on port 8080

---

## 🔥 Available Endpoints

| Method | Endpoint | Description                                            |
|:------|:---------|:-------------------------------------------------------|
| POST | `/movies` | Create a new movie                                     |
| GET | `/movies` | List all movies (pagination + sorting)                 |
| GET | `/movies/{id}` | Get movie by ID                                        |
| PUT | `/movies/{id}` | Update movie                                           |
| DELETE | `/movies/{id}` | Delete movie                                           |
| GET | `/movies/csv` | Download CSV of all movies                             |
| GET | `/movies/poster/{id}` | Get poster URL for movie                               |
| GET | `/movies/aggregate` | Start aggregation (Retrieve movies from external APIs) |

---

## 📦 Postman collection

[Postman-Collection](Maverik.postman_collection.json)

## 📦 Example cURL commands

### Create Movie
```bash
curl -X POST http://localhost:8080/movies \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Inception",
    "year": "2010",
    "actor": "Leonardo DiCaprio",
    "director": "Christopher Nolan",
    "rating": "8.8",
    "posterUrl": "https://example.com/inception.jpg"
  }'
```

### List Movies
```bash
curl -X GET "http://localhost:8080/movies?page=0&size=10&sortBy=title"
```

### Trigger Aggregation (secured)
```bash
curl -X GET http://localhost:8080/movies/aggregate \
  -H "X-API-KEY: supersecretapikey"
```

---

## 🧠 Environment Config (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=moviesdb;TrustServerCertificate=true
    username: sa
    password: SqlS3rv3r23
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver

external:
  movie-api:
    base-url: https://gateway.maverik.com/movie/api
```

---

## 🐳 Docker Compose Overview

```yaml
services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    ports:
      - "1433:1433"
    environment:
      SA_PASSWORD: SqlS3rv3r23
      ACCEPT_EULA: "Y"

  movieservice:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      sqlserver:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:sqlserver://sqlserver:1433;databaseName=moviesdb;TrustServerCertificate=true
      SPRING_DATASOURCE_USERNAME: sa
      SPRING_DATASOURCE_PASSWORD: SqlS3rv3r23
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
```

---