# 🌾 CropWise AI — Mobile App + Backend

A native Android (Jetpack Compose) rebuild of the CropWise AI web app, backed by a
Spring Boot API. It keeps the **same features and mechanisms** as the website:
AI chat advisor, pest/disease diagnosis, crop advisor, weather + climate alerts,
mandi prices, activity log with eco-score, and government schemes — for Indian farmers.

```
app/
├── backend/    Spring Boot (Java 17) — auth + data + AI/weather/market/audio proxy
└── android/    Jetpack Compose app — talks only to the backend
```

## Architecture

The website called external APIs from Next.js server routes and used Firebase for
auth/data. Here the **Spring Boot backend owns everything** — accounts (JWT),
profile, chat history and activity logs (JPA/H2/Postgres) — and proxies the same
external services. The Android app is a thin client.

```
Android (Compose)  ──HTTP/JWT──>  Spring Boot  ──>  Gemini (chat/vision)
                                              ──>  Open-Meteo (weather/soil)
                                              ──>  data.gov.in Agmarknet (mandi)
                                              ──>  Sarvam AI (TTS/STT)
                                              ──>  Neo4j AuraDB (sustainability graph)
```

## Feature → endpoint map

| Feature | Backend endpoint | Android screen |
|---|---|---|
| Auth (signup/login) | `POST /api/auth/register`, `/login` | `LoginScreen` |
| Farm profile (AI context) | `GET/PUT /api/profile` | `ProfileScreen` |
| AI Chat Advisor | `POST /api/gemini` (mode `chat`) | `ChatScreen` |
| Pest & Disease Diagnosis | `POST /api/gemini` (mode `pest_diagnosis`) + Neo4j | `PestDiagnosisScreen` |
| AI Crop Advisor | `POST /api/gemini` (mode `crop_advisor`) | `CropAdvisorScreen` |
| Weather + Climate Alerts | `GET /api/weather` | `WeatherScreen` |
| Mandi Prices (vs MSP) | `GET /api/market` | `MarketScreen` |
| Activity Log + Eco-Score | `GET/POST/DELETE /api/activities`, `/eco-summary` | `ActivityLogScreen` |
| Government Schemes | `GET /api/schemes` | `SchemesScreen` |
| Voice (Indic TTS/STT) | `POST /api/sarvam/tts`, `/stt` | (wired in repository) |
| Knowledge-graph seed | `GET /api/neo4j/seed` | — |

The Gemini system prompts, the pest-diagnosis JSON schema (10 report sections,
organic-first treatments), the eco-score scoring rules, the Open-Meteo field set,
and the data.gov.in Agmarknet grouping logic are ported directly from the website.

## Running the backend

Requires **JDK 17+** and Maven.

```bash
cd app/backend
# (optional) export GEMINI_API_KEY=... MARKET_API_KEY=... SARVAM_API_KEY=... etc.
./mvnw spring-boot:run     # or: mvn spring-boot:run
```

The API starts on `http://localhost:8080`. With no keys set it still runs —
weather/market use live free endpoints, and AI/voice return clear fallback/mock
responses (see `backend/.env.example`).

> No Maven wrapper is checked in. Either install Maven, or generate the wrapper
> with `mvn -N wrapper:wrapper` once.

## Running the Android app

Requires **Android Studio (Koala+)** with the Android SDK.

```bash
# Open app/android in Android Studio, let it sync, then Run on an emulator.
```

The app points at `http://10.0.2.2:8080/` — the emulator's alias for your host's
`localhost`, so it reaches the backend running on your machine. For a physical
device, change `API_BASE_URL` in `android/app/build.gradle.kts` to your machine's
LAN IP. Cleartext HTTP is enabled for local dev.

> No Gradle wrapper jar is checked in. Android Studio generates it on first sync,
> or run `gradle wrapper` in `app/android`.

## Tech stack

**Backend:** Spring Boot 3.3, Spring Security + JWT (jjwt), Spring Data JPA,
H2 (dev) / Postgres (prod), WebClient, Neo4j Java driver.

**Android:** Kotlin 2.0, Jetpack Compose (Material 3), Navigation-Compose,
Retrofit + OkHttp + Gson, DataStore (token), Coil (images).

## Languages supported

English, Hindi, Punjabi, Marathi, Telugu, Tamil — selected in Settings and sent
to the AI and Sarvam voice services, exactly as on the website.
