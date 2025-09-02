# Redis Test Application

A Spring Boot application that demonstrates Redis integration with Docker.

## Prerequisites

- Java 17
- Maven
- Docker

## Setup

### 1. Start Redis with Docker

Run the following command to start Redis in a Docker container:

```bash
docker run -d --name redis-server -p 6379:6379 redis:latest
```

### 2. Build and Run the Application

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Insert Data
**POST** `/api/data`

Request body:
```json
{
  "id": "test-1",
  "name": "Test Data",
  "description": "This is a test data entry",
  "value": 100
}
```

Response:
```json
{
  "success": true,
  "message": "Data inserted successfully",
  "id": "test-1"
}
```

### 2. Get Data
**GET** `/api/data/{id}`

Example: `GET /api/data/test-1`

Response:
```json
{
  "success": true,
  "data": {
    "id": "test-1",
    "name": "Test Data",
    "description": "This is a test data entry",
    "createdAt": "2024-01-15T10:30:00",
    "value": 100
  }
}
```

### 3. Delete Data
**DELETE** `/api/data/{id}`

Example: `DELETE /api/data/test-1`

Response:
```json
{
  "success": true,
  "message": "Data deleted successfully"
}
```

### 4. Health Check
**GET** `/api/health`

Response:
```json
{
  "status": "UP",
  "service": "Redis Test Application"
}
```

## Testing with curl

### Insert test data:
```bash
curl -X POST http://localhost:8080/api/data \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-1",
    "name": "Sample Data",
    "description": "This is sample data for testing",
    "value": 42
  }'
```

### Retrieve data:
```bash
curl http://localhost:8080/api/data/test-1
```

### Delete data:
```bash
curl -X DELETE http://localhost:8080/api/data/test-1
```

## Configuration

Redis connection settings are configured in `src/main/resources/application.properties`:

- Host: localhost
- Port: 6379
- Database: 0
- Timeout: 2000ms

## Features

- JSON serialization for Redis data
- Automatic expiration (1 hour) for stored data
- Error handling with meaningful responses
- Health check endpoint
- RESTful API design
