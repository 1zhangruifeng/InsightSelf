# Testing Notes

## Backend Tests

Local prerequisites:

- JDK 21. Set `JAVA_HOME` and ensure `java` is on `PATH`.
- Maven 3.9 or newer. Ensure `mvn` is on `PATH`.
- Network access the first time Maven restores dependencies.

Run from the backend directory:

```bash
cd backend
mvn clean test
```

Current tests cover:

- Response wrapper and health endpoint.
- Flyway H2 migration startup before JPA repositories are used.
- Flyway migration contract tests for H2 and SQLite, plus static MySQL migration sanity checks.
- Register/login using Spring Security password hashing and server-issued bearer sessions.
- Password storage invariants: adaptive password hashes are persisted, access/refresh token values are not persisted, and legacy SHA-256 password hashes migrate after successful login.
- Session lifecycle: refresh-token rotation revokes the old session, logout revokes the current session, and password change revokes other active sessions while keeping the current session usable.
- Protected endpoint behavior: missing token returns `401`, and cross-user access returns `403`.
- Profile create, get, and update.
- Bazi generation through `6tail/lunar-java`, latest-result lookup, pillars, element scores, calculation method metadata, and structured chart JSON.
- Zodiac daily board determinism, Swiss Ephemeris calculation metadata, natal chart endpoint, and relationship matching.
- Assessment question loading and submission scoring, including the upgraded IPIP-style Big Five, retained MBTI-style inventory, ECR-RS-style attachment form, localized Chinese question text, and submission before a full birth profile exists.
- Assessment validation failures: missing answers, duplicate answers, out-of-range scores, and questions from the wrong instrument all fail with `400`.
- Dashboard auto-generation of Bazi and Zodiac when a profile exists.
- AI report template fallback when `QWEN_API_KEY` is absent.
- AI report API response shape: `generate`, `latest`, and `dashboard.latestAiReport` return display fields only and do not expose internal `sourceSnapshotJson`.
- AI chat session behavior when `QWEN_API_KEY` is absent: a readable service-not-configured assistant message is persisted with the user message, and the returned `sessionId` can be reused.

## Android JVM Tests

Local prerequisites:

- JDK 21. Set `JAVA_HOME` and ensure `java` is on `PATH`.
- Android SDK Platform 35 and Build-Tools 35.x. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`, or create `android/local.properties` with `sdk.dir=...`.
- Network access the first time Gradle restores dependencies.

Run from the Android directory:

```powershell
cd android
.\gradlew.bat testDebugUnitTest assembleDebug
```

Current tests cover:

- OkHttp bearer-token injection: authenticated requests include `Authorization: Bearer ...`, while missing or blank access tokens leave the header unset.
- Auth startup routing from the saved DataStore session: valid saved token plus existing profile opens Main; missing token clears the local session and opens Login.
- Auth startup handling for backend profile status codes: `404` opens onboarding, and `401` clears the local session and opens Login.
- Login routing: username trimming, successful login session persistence, existing-profile routing to Main, missing backend access token errors, and blank input validation without calling the backend.

## Android Manual Test Checklist

Before testing Android, start the backend:

```bash
cd backend
mvn spring-boot:run
```

Then open `android/` in Android Studio and run on an emulator.

Checklist:

- Register a new demo user.
- Login with the same user.
- Confirm missing profile routes to onboarding.
- Create a profile with birth date, birth time, birth place, IANA timezone, latitude, longitude, calendar type, preference, and AI switch.
- Confirm the Home dashboard loads.
- Tap Refresh on Home.
- Open Bazi and generate an insight.
- Confirm the radar chart and four pillars display.
- Open Zodiac and confirm scores display.
- Open Relationship Matching and submit a target birth date.
- Open Assessments and complete Big Five.
- Complete MBTI-style inventory.
- Complete Attachment short form.
- Return to dashboard and confirm assessment snapshot updates.
- Open AI Report and generate a report.
- Confirm report source shows `TEMPLATE` when no Qwen key is configured, or `QWEN` when the backend was started with `QWEN_API_KEY`.
- Export the report and confirm the exported PDF/image contains the same report text shown on screen.
- Open the Home AI chat entry, send two related messages, and confirm the second reply continues the same conversation.
- Logout and confirm the app returns to Login.

## Edge Cases

### Missing Profile

Expected behavior:

- After register/login, Android checks `GET /api/profiles/{userId}`.
- If backend returns profile not found, Android opens onboarding.
- Dashboard, Bazi, Zodiac, and AI report require a profile.
- Assessments require a registered user, but can be submitted before the full profile is created. Without a profile, assessment result text defaults to English.
- AI chat currently requires a profile because its prompt is profile-based.

### Invalid Date

Expected behavior:

- Android onboarding requires a selected birth date in `yyyy-MM-dd`.
- Backend validates required `birthDate`.

### No Assessment Result

Expected behavior:

- Home shows an empty-state message inviting the user to take an assessment.
- Assessment tab shows "No assessment results yet."

### Missing Bazi Result

Expected behavior:

- Bazi tab shows a generate button and a clear message if no latest result exists.
- Dashboard auto-generates Bazi when a profile exists.

### AI API Missing

Expected behavior:

- If `QWEN_API_KEY` is missing, backend generates a template report.
- `generatedBy` is `TEMPLATE`.
- The Android report page still displays the report.
- AI chat returns a readable service-not-configured reply with a non-empty `sessionId`.
- Reusing the returned `sessionId` continues the same backend chat session.

### AI API Configured

Expected behavior:

- Start the backend with `QWEN_API_KEY` set in the backend process environment.
- AI report generation returns `generatedBy=QWEN`.
- `POST /api/ai-reports/generate/{userId}`, `GET /api/ai-reports/latest/{userId}`, and `GET /api/dashboard/{userId}` return report text and source metadata, but do not expose `sourceSnapshotJson`.
- If Qwen is configured but the report provider call fails, report generation returns an explicit upstream error instead of falling back to `TEMPLATE`.
- AI chat returns provider-backed replies rather than the service-not-configured message.
- Reusing the returned chat `sessionId` continues the same multi-turn conversation.
- The backend persists chat history as alternating `user` and `assistant` rows.
- A different authenticated user cannot reuse another user's chat session; the expected status is `403`.

### Network Failure or Backend Unavailable

Expected behavior:

- Android shows network error messages.
- Login, onboarding, dashboard, Bazi, Zodiac, assessments, matching, and AI report screens should not crash.

## Known Limitations

- Authentication is local username/password plus server-side bearer sessions. It is materially stronger than the earlier MVP flow, but still lacks email verification, account recovery, rate limiting, and Android-side automatic refresh-token retry.
- Runtime persistence uses local SQLite with Flyway migrations. MySQL is supported through the `mysql` Spring profile, but production deployments still need explicit backup, restore, and operational migration procedures.
- Bazi uses `6tail/lunar-java`, but the interpretation text remains soft self-reflection language.
- Zodiac daily scores and relationship matching use Swiss Ephemeris planetary positions plus app-defined aspect scoring; they are reflective prompts, not predictions.
- MBTI-style inventory is retained because users recognize it, but it is not the official MBTI instrument and is not a clinical instrument.
- Attachment scoring uses an ECR-RS-style avoidance/anxiety structure on a 1-5 app scale and is not a professional psychological diagnosis.
- AI reports are constrained to soft self-reflection language and should not be treated as medical, legal, financial, psychological, or fortune-telling advice.
- AI report and chat prompts use compact summary context only; raw Bazi/Zodiac `chartJson` and prior report snapshots are kept out of the LLM prompt. The compact report `sourceSnapshotJson` is stored server-side and is not returned to Android.

## Manual Build Notes

Generated folders are intentionally not versioned:

- `backend/target/` is recreated by Maven.
- `backend/ephe/` is a runtime copy of bundled Swiss Ephemeris data files.
- `backend/*.sqlite`, `backend/*.sqlite-wal`, and `backend/*.sqlite-shm` are local SQLite runtime data.
- `android/.gradle/`, `android/build/`, and `android/app/build/` are recreated by Gradle.
- `insightself_mockups/node_modules/` is recreated by npm.

Install these tools locally for a clean checkout:

- JDK 21. Set `JAVA_HOME` and ensure `java` is on `PATH`.
- Maven 3.9 or newer. Ensure `mvn` is on `PATH`.
- Android SDK Platform 35 and Build-Tools 35.x. Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`, or create `android/local.properties` with `sdk.dir=...`.
- Node.js/npm if regenerating mockup screenshots. Ensure `node` and `npm` are on `PATH`.

To run the backend against MySQL instead of the default SQLite database, set `SPRING_PROFILES_ACTIVE=mysql` plus `MYSQL_URL`, `MYSQL_USERNAME`, and `MYSQL_PASSWORD`. The MySQL schema is created by Flyway from `backend/src/main/resources/db/migration/mysql/`.

Android can be built from the command line with:

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

Mockup screenshots can be regenerated with:

```powershell
cd insightself_mockups
npm ci
npm run render
```

Manual emulator validation is still required for final UI flow acceptance because command-line builds do not prove that every screen transition is usable on a device.
