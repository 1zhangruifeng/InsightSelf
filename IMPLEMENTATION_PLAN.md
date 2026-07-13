# InsightSelf Implementation Plan

## 1. Current Status

This repository now contains the full MVP implementation, not only mockups or planning notes.

Current root contents:

- `backend/`: Spring Boot REST API, JPA domain model, repositories, services, controllers, and integration tests.
- `android/`: native Android app using Kotlin, Jetpack Compose, Material 3, Navigation Compose, Retrofit, OkHttp, Coroutines, DataStore, and MVVM-style ViewModels.
- `docs/`: API, database schema, demo script, and testing notes.
- `insightself_mockups/`: HTML prototypes and screenshot references for the mobile UI.

Standard local prerequisites for command-line builds:

- JDK 21 with `JAVA_HOME` set and `java` on `PATH`.
- Maven 3.9 or newer with `mvn` on `PATH`.
- Android SDK Platform 35 and Build-Tools 35.x, exposed through `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or `android/local.properties`.
- Node.js/npm on `PATH` for regenerating mockup screenshots.

## 2. Implemented Backend

Implemented API areas:

- `GET /api/health`
- `POST /api/users/register`
- `POST /api/users/login`
- `POST /api/users/refresh`
- `POST /api/users/logout`
- `PUT /api/users/{userId}/password`
- `POST /api/profiles/{userId}`
- `GET /api/profiles/{userId}`
- `PUT /api/profiles/{userId}`
- `POST /api/bazi/generate/{userId}`
- `GET /api/bazi/latest/{userId}`
- `GET /api/zodiac/daily/{userId}`
- `GET /api/zodiac/natal/{userId}`
- `POST /api/zodiac/match`
- `GET /api/assessments/types`
- `GET /api/assessments/types/{userId}`
- `GET /api/assessments/{type}/questions`
- `GET /api/assessments/{type}/questions/{userId}`
- `POST /api/assessments/{type}/submit`
- `GET /api/assessments/results/{userId}`
- `GET /api/dashboard/{userId}`
- `POST /api/ai-reports/generate/{userId}`
- `GET /api/ai-reports/latest/{userId}`
- `POST /api/ai/chat`

Backend design decisions:

- The backend uses one response wrapper: `{ success, message, data }`.
- The default runtime database is local SQLite; MySQL remains available through the `mysql` Spring profile; integration tests use H2 in memory.
- Flyway owns schema creation with database-specific migrations for SQLite, MySQL, and H2. Hibernate auto-DDL is disabled.
- Authentication uses local username/password plus server-side bearer sessions. Access and refresh tokens are random opaque values, while the database stores only token digests.
- Passwords use Spring Security's delegating password encoder. Legacy SHA-256 hashes are migrated after successful login or password change.
- All user-owned endpoints require a bearer token and reject mismatched `userId` values with `403`.
- Dashboard, Bazi, Zodiac, AI report, and AI chat are profile-driven.
- Assessments require a registered user but do not require a completed birth profile. The profile is used only for language preference in assessment result copy; missing profile defaults to English.
- AI report generation calls Qwen only when `QWEN_API_KEY` is configured; otherwise it creates a deterministic template report.

## 3. Implemented Android App

Implemented screens and flows:

- Startup session routing.
- Login and register.
- Unified profile onboarding.
- Main shell with five bottom tabs: Home, Bazi, Zodiac, Tests, Profile.
- Home dashboard with profile, Bazi, Zodiac, assessment snapshot, integrated summary, latest report entry, and AI chat entry.
- Bazi latest/generate view with element scores and visual components.
- Zodiac daily board and relationship matching.
- Assessment hub, question flow, progress preservation, submission, and result screen.
- AI/template report screen with generate, refresh, export, and share.
- Profile center with profile editing, language switch, password change, and logout.

Android integration decisions:

- Retrofit uses `http://10.0.2.2:8080/` for emulator-to-host backend access.
- DataStore persists the local session with `userId`, `username`, access token, refresh token, token expiry timestamps, and login state. Retrofit injects the access token as a bearer header.
- Android calls only backend APIs for AI-related features.

## 4. Verified Build State

Current verified commands:

```powershell
cd backend
mvn test
```

Result: backend integration tests pass.

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

Result: debug APK builds successfully at `android/app/build/outputs/apk/debug/app-debug.apk`.

## 5. Remaining Work

Highest priority:

1. Complete manual emulator validation through the full `docs/demo_script.md` flow.
2. Decide whether the Zodiac date picker should be fully supported by the backend. The Android UI currently accepts a date, but the backend daily endpoint returns the deterministic board for today.
3. Remove debug `println` calls and unused design placeholder composables from Android code.
4. Replace broad try/catch fallback patterns in AI provider calls and repository layers with explicit failure paths and observable diagnostics.
5. Add Android unit/UI tests around startup routing, onboarding, dashboard loading, assessment submission, and report generation.

Production-readiness backlog:

1. Add rate limiting and account lockout protections for login, register, refresh, and AI endpoints.
2. Add Android-side automatic refresh-token retry and server-side refresh-token reuse detection handling.
3. Add backup/restore guidance and migration runbooks for SQLite and MySQL deployments.
4. Add privacy controls for exporting, deleting, and retaining personal profile data.
5. Add provider timeout policies and audit logging around AI calls.
6. Review all Bazi, Zodiac, MBTI-style, BFI-10, and attachment copy so it stays clearly framed as educational self-reflection rather than diagnosis or prediction.

## 6. Demo Flow

The intended manual demo remains:

1. Start backend with `mvn spring-boot:run`.
2. Launch Android on an emulator.
3. Register a demo user.
4. Complete onboarding.
5. Confirm Home dashboard loads.
6. Generate Bazi insight.
7. Open Zodiac and submit relationship matching.
8. Complete BFI10, MBTI, and ATTACHMENT assessments.
9. Return to Home and refresh dashboard.
10. Generate an AI/template report.
11. Logout and confirm the app returns to Login.
