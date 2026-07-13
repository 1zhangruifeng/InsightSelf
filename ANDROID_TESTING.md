# InsightSelf Android Testing Checklist

Use this checklist for the final emulator demo. Start the backend first, then run the Android app from Android Studio.

## Preflight

Backend:

```powershell
cd backend
mvn clean test
# Optional for provider-backed report/chat smoke tests:
# $env:QWEN_API_KEY = "your-qwen-api-key"
mvn spring-boot:run
```

Android:

```powershell
cd android
.\gradlew.bat testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Confirm the Android Retrofit base URL is:

```text
http://10.0.2.2:8080/
```

## Manual Demo Flow

1. Launch the app on an emulator.
2. Register a new user with username and password; the backend should return a session token and route to onboarding.
3. Confirm successful registration routes to onboarding.
4. Complete onboarding:
   - nickname
   - gender
   - birth date in `yyyy-MM-dd`
   - birth time in `HH:mm:ss`
   - birth place
   - birth timezone such as `Asia/Shanghai`
   - latitude and longitude
   - calendar type `SOLAR` or `LUNAR`
   - preference `EASTERN`, `WESTERN`, or `BALANCED`
   - AI enabled switch
5. Confirm the app routes to Main and opens Home.
6. Confirm Home displays profile, Zodiac, Bazi, assessment snapshot, integrated insight, and report empty state if no report exists.
7. Tap Refresh on Home.
8. Open Bazi and tap Generate Bazi Insight if no result exists.
9. Confirm four pillars and five element score bars display.
10. Open Zodiac and confirm daily emotion, communication, and action scores display.
11. Open Relationship Matching from Zodiac.
12. Submit a target nickname and target birth date.
13. Confirm final score, level, communication tips, risk notes, and collaboration mode display.
14. Open Assessments.
15. Confirm Big Five, MBTI, and ATTACHMENT cards appear. The API type code for Big Five remains `BFI10`.
16. Complete Big Five and confirm the result screen appears.
17. Complete MBTI and confirm the result screen appears.
18. Complete ATTACHMENT and confirm the result screen appears.
19. Return to Home and tap Refresh.
20. Confirm assessment snapshot includes recent results.
21. Open Report.
22. Generate a report.
23. Confirm report text displays.
24. Confirm the report source badge is `TEMPLATE` when `QWEN_API_KEY` is not configured, or `QWEN` when it is configured.
25. Export the report as PDF or image and confirm the exported content matches the report text shown on screen.
26. Return to Home and open the AI chat entry in the header.
27. Send two related messages and confirm the second reply continues the first conversation.
28. Logout.
29. Confirm the app returns to Login and back navigation does not reopen Main.

## Edge Cases

- Backend unavailable: stop Spring Boot and open Home, Bazi, Zodiac, Assessments, or Report. Expected: friendly error state with Retry where appropriate.
- Missing profile: login with a user that has no profile. Expected: route to onboarding.
- Invalid onboarding date: enter a non-`yyyy-MM-dd` date. Expected: validation error, no crash.
- Invalid onboarding time: enter a non-`HH:mm:ss` time. Expected: validation error, no crash.
- Missing Bazi result: open Bazi before generation. Expected: empty state and Generate button.
- No assessment result: open Assessments before submitting. Expected: `No assessment results yet.`
- No latest AI report: open Report before generation. Expected: empty state and Generate button.
- Qwen key missing: generate a report. Expected: report displays with `TEMPLATE`; chat returns a readable service-not-configured reply and a reusable `sessionId`.
- Qwen key configured: generate a report and send two chat messages. Expected: report displays with `QWEN`; chat replies are provider-backed and reuse the same conversation.
- Null backend fields: dashboard cards should show fallback text instead of crashing.
- User session missing: clear app storage or logout. Expected: Login screen.
- App restart after login: relaunch app. Expected: DataStore restores the bearer token, then routes to Main if profile exists or onboarding if profile is missing.
- Expired or invalid backend token: clear the backend database or revoke the session, then relaunch. Expected: app clears local session and returns to Login.

## Expected Safety Wording

InsightSelf should use soft self-reflection language. Bazi, Zodiac, matching, assessments, and reports should avoid medical, legal, financial, psychological diagnosis, or fortune-telling claims.

Expected phrasing includes:

- `may`
- `might`
- `could`
- `for reflection`
- `for course demonstration`
- `This content is for self-reflection and course demonstration only.`
