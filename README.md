# Rate Limiter & Abuse Detection Gateway

A plug-and-play API gateway that sits in front of your backend services and protects them from rate abuse, request floods, and bad actors.

This project combines:

- Configurable rate limiting (per IP, per endpoint)
- Real-time abuse detection based on request behavior
- Automatic blocking of abusive clients
- Reverse proxying to your backend

Built with Spring Boot, Redis, Kafka, and MySQL.

## What problem does this solve?

If you expose APIs publicly, you will face:

- Too many requests from a single client
- Bots hitting endpoints aggressively
- Clients causing repeated errors

This service:

1. Limits how fast clients can call APIs
2. Observes behavior patterns (errors, volume, latency)
3. Calculates an abuse score
4. Temporarily blocks clients that cross the line

No code changes required in your backend.

## High-level flow (simple view)
```
Client → Rate Limiter Gateway → Your Backend
│
├─ Rate limit check (Redis)
├─ Abuse block check (Redis)
├─ Request forwarded if allowed
└─ Event sent to Kafka for scoring
```

## Core Features

### 1. Rate Limiting
- Per-IP + per-endpoint
- Rules stored in MySQL and cached in Redis
- Algorithms supported:
  - Sliding Window Counter
  - Token Bucket (Redis + Lua, atomic)

### 2. Abuse Detection
- Tracks request volume and error rate
- Calculates a rolling abuse score
- Automatically blocks IPs that exceed threshold
- Block is temporary (auto-expires)

### 3. Reverse Proxy
- Gateway forwards allowed requests to your backend
- Backend URL is configurable

### 4. Fully Containerized
- Docker + Docker Compose
- Redis, Kafka, MySQL included


## Tech Stack

- Java 25 + Spring Boot
- Redis (rate limiting + abuse state)
- Kafka (async abuse processing)
- MySQL (rule persistence)
- WebClient (Reactive) for proxying


## Project Structure (important parts)
```
config/          → Redis & WebClient config
controller/      → API endpoints (rules + proxy)
filter/          → Rate limiting filter (core gatekeeper)
limiter/         → Rate limiting algorithms
model/           → DTOs and DB entities
service/         → Business logic
utils/           → Lua scripts
```

## How Rate Limiting Works

### Request key
- Client IP address (`request.getRemoteAddr()`)
- Endpoint path (`request.getRequestURI()`)

### Decision flow
1. Check if IP is already blocked due to abuse
2. Load rate limit rule for endpoint
3. Apply algorithm
4. Allow or reject request

If rejected → **HTTP 429**


## Supported Rate Limiting Algorithms

### 1. Sliding Window Counter
- Smooth rate limiting
- Uses current + previous window
- Supports in-memory and Redis (Lua)

### 2. Token Bucket (Redis)
- Tokens refill over time
- Atomic via Lua scripts
- Safe under high concurrency


## Abuse Detection Logic

Every request generates an event with:
- IP
- Endpoint
- Status code
- Latency

These events are:
1. Sent to Kafka
2. Consumed asynchronously
3. Scored in Redis

### Scoring rules (simplified)
- Too many requests → score increases
- High error rate → score increases faster
- Score > 50 → IP is blocked for 15 minutes


## API Endpoints

### Create a Rate Limit Rule

**POST** `/ratelimiter/create-rule`

**Body:**
```json
{
  "limit": 100,
  "windowMillis": 60000,
  "endpoint": "/api/orders",
  "author": "admin",
  "useTokenBucket": true
}
```


### Update an Existing Rule

**POST** `/ratelimiter/update-rule/{id}`

**Body:**
```json
{
  "limit": 200,
  "windowMillis": 60000,
  "endpoint": "/api/orders",
  "author": "admin",
  "useTokenBucket": false
}
```


### Proxy Endpoint (Catch-all)

`/**`

- Any request not matching `/ratelimiter/*`
- Automatically forwarded to backend
- Rate limiting + abuse checks applied


## HTTP Responses

| Situation            | Response                         |
|---------------------|----------------------------------|
| Allowed             | Forwarded response               |
| Rate limit exceeded | 429 Rate Limit Exceeded          |
| Abuse blocked       | 429 Your request has been blocked|


## Running the Project

### Using Docker (recommended)

```bash
docker-compose up --build
```

Services started:
- MySQL (3306)
- Redis (6379)
- Kafka (9092)
- Rate Limiter Gateway (9000)


## Backend Service

Your actual backend should run separately, for example:

`http://localhost:8080`

All requests to `http://localhost:9000/**` will be proxied there.


## Why this design works

- Redis + Lua → safe under heavy concurrency
- Kafka → no latency added to request path
- Rules in DB + cache → dynamic updates without restart
- Single filter → clean, centralized enforcement


## Things intentionally kept simple

- No auth layer (easy to add later)
- No UI (API-first)
- No complex rule matching (path-based only)


## Notes

- Designed to be readable and extensible
- Easy to add:
  - Per-user limits
  - Role-based rules
  - Dashboard
 
## Author
Built by Devansh as a deep dive into rate limiter and distributed systems.
