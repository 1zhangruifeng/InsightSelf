# InsightSelf 2.0

[English](README.md) | [简体中文](README.zh-CN.md)

> A bilingual Android self-reflection platform that brings personality assessments, Bazi, Western astrology, daily reflection, and AI-assisted reporting into one coherent experience.

InsightSelf was created in response to a familiar problem: young people are highly interested in personality types, zodiac content, and self-understanding, but their results are usually scattered across unrelated apps, short videos, screenshots, and one-off tests.

InsightSelf turns that fragmented interest into a structured journey. A single user profile powers multiple reflection modules, preserves past results, and produces an integrated summary with clear safety boundaries. The product is designed for reflection and communication, not diagnosis or deterministic prediction.

## Highlights

- **One profile, multiple modules** - birth details, language, and preferences are entered once and reused throughout the app.
- **Bilingual experience** - the main workflow and generated summaries support English and Simplified Chinese.
- **Structured reflection** - Bazi, Zodiac, assessments, daily insights, and reports are connected instead of presented as isolated results.
- **Recognisable assessments** - includes IPIP-style Big Five, MBTI-style preferences, O*NET Mini Interest Profiler, WHO-5, Rosenberg Self-Esteem, and attachment reflection.
- **AI is optional** - Qwen can generate a richer integrated report, while a deterministic local template keeps the core demo usable without a paid API.
- **Privacy-conscious demo** - the built-in demo account contains demonstration data only and no real personal information.
- **Reproducible local setup** - Spring Boot, SQLite, Flyway migrations, and an Android emulator are enough to run the complete prototype.

## Technology Stack

| Layer | Technology |
| --- | --- |
| Android app | Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit, OkHttp, DataStore |
| Architecture | MVVM-style ViewModels, repository layer, REST API |
| Backend | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Database | SQLite by default, Flyway migrations, optional MySQL profile |
| Domain engines | `6tail/lunar-java`, Swiss Ephemeris Java |
| Reports | Local template fallback or optional Qwen-compatible API |
| Testing | Spring Boot integration tests, JUnit, MockWebServer |

## Repository Structure

```text
InsightSelf/
|-- android/                 # Native Android application
|-- backend/                 # Spring Boot REST API
|-- docs/                    # API, database, testing, and demo notes
|-- insightself_mockups/     # UI design mockups
|-- ANDROID_TESTING.md       # Android test notes
|-- THIRD_PARTY_NOTICES.md   # Dependency and instrument notices
|-- LICENSE
`-- README.md
```

## Quick Start

The simplest supported setup is:

```text
Windows/macOS/Linux host
|-- Spring Boot backend: http://localhost:8080
`-- Android Studio emulator: http://10.0.2.2:8080
```

No MySQL server or AI key is required for the default demo.

### 1. Prerequisites

Install:

- **JDK 21**
- **Maven 3.8+**
- **Android Studio** with Android SDK Platform 35 and an Android emulator
- Git

The project includes the Gradle wrapper, so a separate Gradle installation is not required.

Check the command-line tools:

```powershell
java -version
mvn -version
git --version
```

In Android Studio, install **Android SDK Platform 35** from `Tools > SDK Manager` if it is not already available.

### 2. Clone the Repository

Copy the HTTPS address from the GitHub repository's **Code** button, then run:

```powershell
git clone <repository-url>
cd InsightSelf
```

### 3. Start the Backend

Open a terminal in the repository root:

```powershell
cd backend
mvn spring-boot:run
```

Wait until the console reports that Tomcat has started on port `8080`. Verify the service in a browser:

```text
http://localhost:8080/api/health
```

The first run automatically creates and migrates the local database:

```text
backend/insightself.sqlite
```

This runtime database is ignored by Git.

### 4. Start the Android App

1. Open Android Studio.
2. Select **Open** and choose the repository's `android/` folder.
3. Wait for Gradle Sync to finish.
4. Start an Android emulator, preferably API 33 or newer.
5. Select the `app` run configuration and click **Run**.

The emulator build connects to the local backend through:

```text
http://10.0.2.2:8080/
```

`10.0.2.2` is Android Emulator's special alias for the host computer's `localhost`.

### 5. Enter the Demo

On the login screen, use the built-in demo entry. The backend will create or refresh a demonstration account and sign in automatically.

The account is seeded with a fictional Hong Kong profile, completed assessment examples, Bazi and Zodiac data, dashboard content, and an integrated report. It contains **no real personal information**.

You can also register a new account and complete the full onboarding flow yourself.

## Command-Line Build and Tests

### Backend

Run all backend tests:

```powershell
cd backend
mvn clean test
```

Build the backend package:

```powershell
cd backend
mvn clean package
```

Run the packaged application:

```powershell
java -jar target/insightself-backend-0.0.1-SNAPSHOT.jar
```

### Android

Build a debug APK on Windows:

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

Run Android unit tests:

```powershell
cd android
.\gradlew.bat :app:testDebugUnitTest
```

The debug APK is generated under:

```text
android/app/build/outputs/apk/debug/
```

On macOS or Linux, replace `.\gradlew.bat` with `./gradlew`.

## Optional AI Configuration

The application is fully demonstrable without an external AI service. When `QWEN_API_KEY` is absent, integrated reports use the local `TEMPLATE` generator. AI chat clearly reports that the service is not configured.

To enable Qwen, set environment variables **before** starting the backend:

```powershell
$env:QWEN_API_KEY = "your-api-key"
$env:QWEN_MODEL = "qwen-plus"
$env:QWEN_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
mvn spring-boot:run
```

Never place API keys in source files or commit them to GitHub.

## Optional MySQL Configuration

SQLite is recommended for local use. To use MySQL instead, first create an empty `insightself` database, then run:

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/insightself?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "your-password"
cd backend
mvn spring-boot:run
```

Flyway applies the appropriate migrations for SQLite, MySQL, or the H2 test database.

## Using a Physical Android Device

The default Android configuration targets the Android emulator. A physical phone cannot use `10.0.2.2`.

For development on a physical device:

1. Connect the phone and computer to the same network.
2. Find the computer's LAN IPv4 address, for example `192.168.1.20`.
3. Change `BASE_URL` in `android/app/src/main/java/com/example/insightself/data/api/RetrofitClient.kt` to `http://192.168.1.20:8080/`.
4. Allow Java/Spring Boot through the host firewall when prompted.
5. Rebuild and run the app.

For a public release, use an HTTPS backend and disable cleartext HTTP traffic.

## Product Flow

1. Register or load the built-in demo account.
2. Create one unified profile.
3. View the integrated home dashboard and daily reflection.
4. Explore Bazi five-element patterns.
5. Explore Zodiac, natal placements, and daily aspects.
6. Complete structured assessments and review their reports.
7. Generate an integrated report and export or share it.
8. Switch between English and Simplified Chinese from Profile settings.

## Main API Areas

```text
GET  /api/health             Public health check
POST /api/users/register     Register
POST /api/users/login        Login
POST /api/demo/seed          Seed and sign in to the built-in demo
GET  /api/profiles/{userId}  Unified profile
GET  /api/dashboard/{userId} Integrated dashboard
POST /api/bazi/generate/{userId}
GET  /api/zodiac/daily/{userId}
GET  /api/assessments/types/{userId}
POST /api/ai-reports/generate/{userId}
POST /api/ai/chat            Context-aware chat
```

See [`docs/api_overview.md`](docs/api_overview.md) for the detailed API guide and [`docs/database_schema.md`](docs/database_schema.md) for the data model.

## Safety and Scope

InsightSelf is an educational self-reflection product. Its Bazi, Zodiac, relationship, assessment, AI, and report features:

- do not provide medical or psychological diagnosis;
- do not replace professional advice;
- do not claim deterministic prediction;
- should be interpreted as prompts for reflection and communication;
- require informed handling of birth and assessment data in any real deployment.

The MBTI-style module is a familiar preference reflection experience and is **not** the proprietary official MBTI instrument. See [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) for dependency, dataset, and questionnaire notices.

## Troubleshooting

### Android shows a network or server error

- Confirm the backend terminal is still running.
- Open `http://localhost:8080/api/health` on the computer.
- Confirm the emulator uses `http://10.0.2.2:8080/`, not `localhost`.
- Restart the app after the backend becomes ready.

### Port 8080 is already in use

Stop the other process using port `8080`. The Android base URL also needs to be changed if the backend is moved to another port.

### Android Studio cannot find the SDK

Open `android/` as the Android Studio project. Android Studio normally creates `android/local.properties` automatically. For command-line builds, set `ANDROID_HOME` or `ANDROID_SDK_ROOT`.

### Android unit tests report `GradleWorkerMain` on Windows

If the APK builds but Gradle's test executor cannot find `GradleWorkerMain`, place `GRADLE_USER_HOME` in a short ASCII-only path and rerun the tests. This can occur when a Windows user-profile path contains characters that a worker process does not decode correctly.

```powershell
$env:GRADLE_USER_HOME = "C:\gradle-home"
.\gradlew.bat --no-daemon --max-workers=1 :app:testDebugUnitTest
```

### Backend database migration fails after an older build

Back up any data you need, stop the backend, remove the local ignored `backend/insightself.sqlite` file, and start the backend again. Flyway will create a clean migrated database.

### Reports show `TEMPLATE`

This is expected when no Qwen key is configured. The local template is an intentional offline-compatible fallback, not an error.

## Further Documentation

- [`docs/demo_script.md`](docs/demo_script.md) - suggested product demonstration flow
- [`docs/testing_notes.md`](docs/testing_notes.md) - test scope and verification notes
- [`ANDROID_TESTING.md`](ANDROID_TESTING.md) - Android testing instructions
- [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) - implementation background
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) - third-party notices

## Before Publishing This Repository

The repository is configured to ignore local databases, IDE settings, build output, API environment files, and Android SDK paths. Before pushing, still review the staged files and confirm that no secrets or personal data are included:

```powershell
git status
git diff --cached
```

Recommended repository topics:

```text
android  kotlin  jetpack-compose  spring-boot  sqlite  self-reflection  wellness
```

## License

This project is licensed under **AGPL-3.0-or-later**. See [`LICENSE`](LICENSE) and [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) before redistribution or deployment.
