# Running CropWise AI — setup

Keys are already wired in. You need a JDK 17+, Maven, and Android Studio.

## Backend (Spring Boot)

You don't strictly need a Maven wrapper — a system Maven works:

```bash
cd app/backend
mvn spring-boot:run
```

Starts on http://localhost:8080. Quick check:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"farmer@test.com","password":"secret123","name":"Test"}'
# → returns { "token": "...", "user": {...} }

curl "http://localhost:8080/api/weather?state=Punjab"
# → live Open-Meteo data
```

### (Optional) generate the Maven wrapper

If you want `./mvnw` so collaborators don't need Maven installed, run this once on a
machine that has Maven (it writes `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`):

```bash
cd app/backend
mvn -N wrapper:wrapper -Dmaven=3.9.9
```

## Android (Jetpack Compose)

The Gradle wrapper **jar is generated automatically** the first time you open the
project in Android Studio (the `gradle/wrapper/gradle-wrapper.properties` that
controls the Gradle version is already committed). So:

1. Open `app/android` in Android Studio (Koala or newer).
2. Let it sync — it downloads Gradle 8.9 and creates `gradlew` + the wrapper jar.
3. Run on an emulator. Start the backend first; the app targets
   `http://10.0.2.2:8080` (the emulator's alias for your host's localhost).

### (Optional) generate the wrapper from the CLI instead

If you have Gradle installed and prefer the terminal:

```bash
cd app/android
gradle wrapper --gradle-version 8.9
./gradlew :app:assembleDebug
```

For a physical device, set `API_BASE_URL` in `app/android/app/build.gradle.kts`
to your computer's LAN IP (e.g. `http://192.168.1.50:8080/`).

## Why no wrappers are committed

They were generated in a sandbox with no Android SDK and no network access to
Maven Central / Gradle services, so the binary wrapper artifacts couldn't be
produced. The two commands above recreate them in seconds on your machine.
