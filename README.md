# Redis Test Application

A Spring Boot application for testing Redis performance with different approaches and servers.

## Features

- **RedisTemplate Approach**: Manual Redis operations using RedisTemplate
- **Spring Data Redis Approach**: Repository-based operations using Spring Data Redis
- **Performance Testing**: Concurrent insertion and monitoring with real-time statistics
- **Single Server Testing**: Test different approaches on the same Redis server
- **Comparison Testing**: Compare performance between different Redis approaches using different key prefixes

## Setup

### 1. Start Redis Server

```bash
docker run -d --name redis-local -p 6379:6379 redis:latest
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

## API Endpoints

### Basic Operations

#### RedisTemplate Approach
- `POST /api/data` - Insert data using RedisTemplate
- `GET /api/data/{id}` - Get data by ID
- `GET /api/data/all` - Get all data
- `DELETE /api/data/{id}` - Delete data by ID

#### Spring Data Redis Approach
- `POST /api/spring-data/data` - Insert data using Spring Data
- `GET /api/spring-data/data/{id}` - Get data by ID
- `GET /api/spring-data/data/all` - Get all data
- `DELETE /api/spring-data/data/{id}` - Delete data by ID

### Performance Testing

#### RedisTemplate Performance Test
- `POST /api/performance/start?records=100000` - Start performance test
- `GET /api/performance/status` - Get test status
- `DELETE /api/performance/clear` - Clear test data

#### Spring Data Performance Test
- `POST /api/spring-data/performance/start?records=100000` - Start performance test
- `GET /api/spring-data/performance/status` - Get test status
- `DELETE /api/spring-data/performance/clear` - Clear test data

### Comparison Testing
- `POST /api/comparison/start?records=10000` - Start comparison test (all approaches)
- `GET /api/comparison/status` - Get comparison test status
- `DELETE /api/comparison/clear` - Clear all test data

## Testing Different Redis Approaches

### Configuration

The application supports testing different Redis approaches on the same server by using different key prefixes:

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.database=0
```

### Testing Scenarios

#### 1. Individual Approach Testing
```bash
# Test RedisTemplate approach
curl -X POST "http://localhost:8080/api/performance/start?records=10000"

# Test Spring Data approach
curl -X POST "http://localhost:8080/api/spring-data/performance/start?records=10000"
```

#### 2. Comparison Testing (Same Redis Server)
```bash
# Start comparison test (tests both approaches simultaneously on same Redis)
curl -X POST "http://localhost:8080/api/comparison/start?records=10000"

# Monitor progress
curl -s http://localhost:8080/api/comparison/status | python3 -m json.tool
```

#### 3. Real-time Monitoring
```bash
# Monitor comparison test every 0.5 seconds
for i in {1..20}; do 
    echo "=== Monitoring #$i ==="; 
    curl -s http://localhost:8080/api/comparison/status | python3 -m json.tool; 
    sleep 0.5; 
done
```

## Performance Comparison

The application provides two different approaches for Redis operations:

1. **RedisTemplate**: Direct Redis operations with manual serialization
2. **Spring Data Redis**: Repository-based operations with automatic serialization

### Expected Performance Characteristics

- **RedisTemplate**: Generally faster for simple operations, more control over serialization
- **Spring Data Redis**: More convenient, automatic serialization, slightly more overhead
- **Same Server**: Both approaches use the same Redis instance with different key prefixes for isolation

## Docker Commands

### Start Redis Server
```bash
docker run -d --name redis-local -p 6379:6379 redis:latest
```

### Stop Redis Server
```bash
docker stop redis-local
docker rm redis-local
```

### Clear Redis Data
```bash
# Clear Redis data
docker exec redis-local redis-cli FLUSHALL
```

## Monitoring and Logs

The application provides detailed logging for:
- Insertion rates (records per second)
- Real-time monitoring every 0.5 seconds
- Performance comparisons between approaches
- Error handling and recovery

Check the application logs for detailed performance metrics and monitoring information.

## Configuration Options

### Redis Connection Pool
```properties
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
```

### Timeout Settings
```properties
spring.data.redis.timeout=2000ms
```

## Troubleshooting

### Common Issues

1. **Connection Refused**: Ensure Redis servers are running on correct ports
2. **Serialization Errors**: Check that TestData model is properly configured
3. **Performance Issues**: Adjust connection pool settings and timeout values

### Health Check
```bash
curl http://localhost:8080/api/health
```

## Architecture

The application uses:
- **Spring Boot 3.5.5** for the main framework
- **Spring Data Redis** for repository operations
- **Lettuce** as the Redis client
- **Jackson** for JSON serialization
- **Lombok** for reducing boilerplate code