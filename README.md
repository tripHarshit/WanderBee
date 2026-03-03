# WanderBee

A full-stack travel companion application with an Android client built using Jetpack Compose and a Spring Boot microservices backend orchestrated with Docker Compose.

---

## System Architecture

```
+---------------------------------------------------------------------+
|                         Android Client                              |
|              Jetpack Compose  -  MVVM  -  Hilt  -  Retrofit         |
+-----------------------------------+---------------------------------+
                                    |
                           HTTP / WebSocket (STOMP)
                                    |
+-----------------------------------v---------------------------------+
|                       API Gateway  (:8082)                           |
|              Spring Cloud Gateway  -  JWT Validation                |
+-----------+--------------+--------------+---------------------------+
            |              |              |
            v              v              v
    +---------------+ +------------+ +------------+
    |   Identity    | | Destination| |    Chat    |
    |   Service     | |  Service   | |   Service  |
    |   (:8083)     | |  (:8084)   | |   (:8085)  |
    +-------+-------+ +-----+--+---+ +---+----+---+
            |               |  |          |    |
            v               v  v          v    v
     +-----------+  +--------+ +-----+ +-------+ +-------+
     | PostgreSQL|  |Postgres| |Redis| |MongoDB| | Kafka |
     | (identity)|  |(destn.)|       |         |         |
     +-----------+  +--------+ +-----+ +-------+ +-------+

          All services register with Eureka Discovery Server (:8081)
```

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Android UI** | Jetpack Compose, Material 3, Coil, Lottie |
| **Android Architecture** | MVVM, Hilt DI, Kotlin Coroutines & Flow, Room DB |
| **Networking** | Retrofit + OkHttp, STOMP over WebSocket |
| **Authentication** | JWT (backend-issued), Google Sign-In, Firebase Auth (notifications only) |
| **Backend Framework** | Spring Boot 4, Spring Cloud Gateway, Eureka Discovery |
| **Databases** | PostgreSQL 16 (identity & destinations), MongoDB 7 (chat), Redis 7 (cache) |
| **Messaging** | Apache Kafka (chat message event streaming) |
| **AI** | Google Gemini (city descriptions, cultural tips, itinerary generation) |
| **Media APIs** | Pexels API (photos & videos), OpenWeather API (weather data) |
| **Containerization** | Docker Compose (full backend stack) |

---

## Project Structure

```
WanderBee/
|
+-- app/                                    # Android application
|   +-- src/main/java/.../wanderbee/
|       +-- data/
|       |   +-- cache/                      # In-memory caches (descriptions, cultural tips)
|       |   +-- local/                      # Room DB entities, DAOs, database migrations
|       |   +-- remote/
|       |   |   +-- apiService/             # Retrofit API interfaces
|       |   |   |   +-- ChatApiService          # Chat rooms & messages
|       |   |   |   +-- DestinationApiService    # Destinations, search, save/unsave
|       |   |   |   +-- GenerationService        # AI content generation
|       |   |   |   +-- IdentityApiService       # Auth, profile, token validation
|       |   |   |   +-- PexelsApiService         # Photos & videos
|       |   |   |   +-- WeatherApiService        # Weather data
|       |   |   +-- models/                 # DTOs (auth, chat, destinations, AI)
|       |   |   +-- AuthInterceptor.kt      # JWT token injection into requests
|       |   |   +-- RetrofitInstance.kt      # HTTP client configuration
|       |   |   +-- StompClient.kt          # WebSocket STOMP client for real-time chat
|       |   +-- repository/                 # Repository interfaces & implementations
|       +-- di/                             # Hilt DI modules (App, Network, Repository)
|       +-- navigation/                     # Compose Navigation graph
|       +-- screens/
|       |   +-- authentication/             # Login, Signup, Forgot Password
|       |   +-- chat/                       # Group chat, Private chat, All chats list
|       |   +-- details/                    # Destination info, photos, videos tabs
|       |   +-- home/                       # Search bar, popular & Indian destinations
|       |   +-- itinerary/                  # AI-generated travel itineraries
|       |   +-- onboarding/                 # First-launch onboarding flow
|       |   +-- profile/                    # User profile management
|       |   +-- saved/                      # Saved/bookmarked destinations
|       |   +-- splash/                     # Splash screen with animations
|       +-- services/                       # FCM push notification service
|       +-- ui/theme/                       # Colors, typography, theme configuration
|       +-- utils/                          # Resource wrapper, AppPreferences, helpers
|
+-- WanderBee-Backend/                      # Spring Boot microservices
    +-- docker-compose.yml                  # Full stack orchestration (all services + infra)
    +-- api-gateway/                        # Spring Cloud Gateway (:8082)
    +-- discovery-server/                   # Eureka Service Registry (:8081)
    +-- identity-service/                   # Auth & user management (:8083)
    |   +-- controller/                     # AuthController (login, register, Google OAuth)
    |   +-- entity/                         # UserCredentials (JPA entity)
    |   +-- service/                        # AuthService, JwtService, GoogleAuthService
    +-- destination-service/                # Destinations & AI generation (:8084)
    |   +-- destination/
    |   |   +-- controller/                 # DestinationController (CRUD, search, save)
    |   |   +-- model/                      # City, SavedDestination (JPA entities)
    |   |   +-- service/                    # DestinationService (Redis-cached queries)
    |   +-- generation/
    |       +-- controller/                 # AI generation endpoints
    |       +-- service/                    # Gemini AI integration service
    +-- chat-service/                       # Real-time chat (:8085)
        +-- config/                         # WebSocket/STOMP config, Kafka config
        +-- controller/                     # ChatController (rooms, messages)
        +-- model/                          # MongoDB documents (ChatRoom, ChatMessage)
        +-- service/                        # ChatService, Kafka producer/consumer
```

---

## Backend Microservices

### Discovery Server (Eureka) - Port 8081
Service registry. All microservices register here and discover each other dynamically at runtime.

### API Gateway - Port 8082
Single entry point for the Android client. Routes requests to the appropriate microservice, validates JWT tokens, and handles cross-cutting concerns.

| Route Pattern | Target Service |
|---------------|----------------|
| `/api/v1/auth/**` | Identity Service |
| `/api/v1/destinations/**` | Destination Service |
| `/api/v1/generation/**` | Destination Service |
| `/api/v1/chat/**` | Chat Service |
| `/ws/**` | Chat Service (WebSocket) |

### Identity Service - Port 8083
User authentication and JWT token lifecycle.

- Email/password registration and login
- Google Sign-In via ID token exchange
- JWT token generation and validation
- PostgreSQL storage for user credentials

### Destination Service - Port 8084
Manages destination data, search, bookmarking, and AI-powered content.

- City search with pagination
- Popular destinations and Indian destinations (curated lists)
- Save/unsave destinations per user
- AI-generated city descriptions, cultural tips, and itineraries (Gemini)
- Weather data via OpenWeather API
- Photos and videos via Pexels API
- Redis caching for frequently accessed data

### Chat Service - Port 8085
Real-time messaging powered by WebSocket and Kafka.

- Group chat rooms (destination-specific)
- Private 1:1 messaging
- STOMP over WebSocket for real-time message delivery
- Kafka for message event streaming
- MongoDB for chat history and room storage

---

## API Endpoints

### Identity Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register with email and password |
| POST | `/api/v1/auth/token` | Login and receive JWT |
| POST | `/api/v1/auth/google` | Google Sign-In token exchange |
| GET | `/api/v1/auth/validate` | Validate JWT token |

### Destination Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/destinations/popular` | Get popular destinations |
| GET | `/api/v1/destinations/indian` | Get Indian destinations |
| GET | `/api/v1/destinations/all` | Get all destinations |
| GET | `/api/v1/destinations/search?query=` | Search destinations by name |
| POST | `/api/v1/destinations/save` | Save a destination |
| DELETE | `/api/v1/destinations/save/{cityId}` | Unsave a destination |
| GET | `/api/v1/destinations/saved/{userId}` | Get user's saved destinations |

### Generation Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/generation/description?city=` | AI-generated city description |
| GET | `/api/v1/generation/cultural-tips?city=` | AI-generated cultural tips |
| POST | `/api/v1/generation/itinerary` | AI-generated day-by-day itinerary |

### Chat Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/chat/rooms/group` | Create a group chat room |
| POST | `/api/v1/chat/rooms/private` | Create or get a private chat |
| GET | `/api/v1/chat/rooms/user/{userId}` | Get all chat rooms for a user |
| GET | `/api/v1/chat/rooms/{roomId}/messages` | Get messages in a room |
| WS | `/ws` | STOMP WebSocket endpoint |

---

## Features

| Feature | Description |
|---------|-------------|
| **Destination Discovery** | Browse popular cities, Indian destinations, and search globally |
| **AI Descriptions** | Gemini-generated city overviews with key highlights |
| **AI Cultural Tips** | Local customs, etiquette, and travel advice generated by AI |
| **AI Itineraries** | Day-by-day travel plans with activities and timings |
| **Photo & Video Gallery** | High-quality destination media from Pexels |
| **Weather Info** | Real-time weather data for any destination |
| **Real-time Chat** | STOMP WebSocket group and private messaging |
| **Save Destinations** | Bookmark cities with local Room DB + backend sync |
| **Google Sign-In** | One-tap authentication via Google OAuth |
| **Push Notifications** | Firebase Cloud Messaging for alerts and updates |
| **Offline Support** | Room DB caching for offline access to saved data |
| **Onboarding** | Animated first-launch walkthrough |

---

## Android Architecture

```
+--------------+     +--------------+     +--------------+
|    Screen    | --> |  ViewModel   | --> |  Repository  |
|  (Compose)   | <-- |  (StateFlow) | <-- |  (Interface) |
+--------------+     +--------------+     +------+-------+
                                                 |
                            +--------------------+--------------------+
                            v                    v                    v
                     +------------+      +-------------+     +--------------+
                     |  Retrofit  |      |   Room DB   |     | Memory Cache |
                     |  (Remote)  |      |   (Local)   |     |   (In-mem)   |
                     +------------+      +-------------+     +--------------+
```

- **MVVM** pattern with ViewModel + Kotlin StateFlow for reactive UI
- **Hilt** dependency injection across all layers
- **Repository pattern** abstracts remote API, local database, and in-memory cache
- **AuthInterceptor** automatically attaches JWT token to all outgoing API requests
- **AppPreferences** (Jetpack DataStore) stores user session data (token, email, name)

---

## Infrastructure (Docker Compose)

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| PostgreSQL (identity) | `postgres:16-alpine` | 5433 | User credentials storage |
| PostgreSQL (destination) | `postgres:16-alpine` | 5434 | Destination data storage |
| MongoDB | `mongo:7.0` | 27017 | Chat messages and rooms |
| Redis | `redis:7-alpine` | 6379 | Destination query caching |
| Zookeeper | `confluentinc/cp-zookeeper:7.6.0` | 2181 | Kafka coordination |
| Kafka | `confluentinc/cp-kafka:7.6.0` | 9092 | Chat message event streaming |
| Kafka UI | `provectuslabs/kafka-ui` | 8090 | Kafka monitoring dashboard |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 24+ (target SDK 35)
- JDK 21
- Docker and Docker Compose
- Firebase project (for push notifications)

### 1. Start the Backend

```bash
cd WanderBee-Backend
docker-compose up -d
```

Wait approximately 60 seconds for all services to become healthy. Verify with:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

Dashboards:
- Eureka: http://localhost:8081
- Kafka UI: http://localhost:8090
- API Gateway: http://localhost:8082

### 2. Configure the Android App

1. Place your `google-services.json` in the `app/` directory.
2. Add API keys to `local.properties`:
   ```properties
   PEXELS_API_KEY=your_pexels_api_key
   OPENWEATHER_API_KEY=your_openweather_api_key
   ```
3. The app connects to the backend at `http://10.0.2.2:8082/` (Android emulator loopback to host machine).

### 3. Build and Run

Open the project in Android Studio and run on an emulator or physical device.

---

## Design

- Dark theme with yellow (`#EEB644`) and cyan (`#19DCFF`) accents
- Istok Web and Coustard font families
- Smooth 300ms animations with EaseOutBack and EaseOutCubic easing
- Card-based layouts with gradient backgrounds

---

## License

This project is for educational purposes.
