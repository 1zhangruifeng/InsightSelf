# API Overview

All backend responses use this wrapper:

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

For errors, `success` is `false`, `message` contains the reason, and `data` is `null`.

Base URL for local backend:

```text
http://localhost:8080
```

Base URL from Android emulator:

```text
http://10.0.2.2:8080/
```

## Authentication

Public endpoints:

- `GET /api/health`
- `POST /api/users/register`
- `POST /api/users/login`
- `POST /api/users/refresh`

All other endpoints require a bearer access token:

```http
Authorization: Bearer <accessToken>
```

The server stores only token hashes in `auth_sessions`. Business endpoints that contain `userId` in the path or body also verify that the requested `userId` matches the authenticated token owner. A missing or expired token returns `401`; a token for a different user returns `403`.

## Health

### GET `/api/health`

Purpose: Check whether the backend is running.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "status": "UP",
    "service": "InsightSelf Backend",
    "time": "2026-05-03T16:50:57"
  }
}
```

## Users

### POST `/api/users/register`

Purpose: Create a demo user account.

Request example:

```json
{
  "username": "demo",
  "password": "pass1234"
}
```

Response example:

```json
{
  "success": true,
  "message": "registered",
  "data": {
    "userId": 1,
    "username": "demo",
    "createdAt": "2026-05-03T16:30:00",
    "accessToken": "random-access-token",
    "refreshToken": "random-refresh-token",
    "accessTokenExpiresAt": "2026-05-04T04:30:00Z",
    "refreshTokenExpiresAt": "2026-06-02T16:30:00Z"
  }
}
```

### POST `/api/users/login`

Purpose: Login with username and password.

Request example:

```json
{
  "username": "demo",
  "password": "pass1234"
}
```

Response example: same authenticated user payload as registration, with fresh access and refresh tokens.

### POST `/api/users/refresh`

Purpose: Rotate a refresh token and issue a fresh access/refresh pair. The previous session is revoked.

Request example:

```json
{
  "refreshToken": "random-refresh-token"
}
```

Response example: same authenticated user payload as registration.

### POST `/api/users/logout`

Purpose: Revoke the currently authenticated bearer session.

Headers: requires `Authorization: Bearer <accessToken>`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": true
}
```

### PUT `/api/users/{userId}/password`

Purpose: Change the authenticated user's password after verifying the old password. Other active sessions for the same account are revoked; the current session remains valid.

Headers: requires `Authorization: Bearer <accessToken>`.

Request example:

```json
{
  "oldPassword": "pass1234",
  "newPassword": "newpass1234"
}
```

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": true
}
```

## Profiles

### POST `/api/profiles/{userId}`

Purpose: Create or replace the unified profile for a user.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Request example:

```json
{
  "nickname": "Alex",
  "gender": "Prefer not to say",
  "birthDate": "2001-08-18",
  "birthTime": "09:30:00",
  "birthPlace": "Hong Kong",
  "birthTimezone": "Asia/Hong_Kong",
  "latitude": 22.3193,
  "longitude": 114.1694,
  "calendarType": "SOLAR",
  "preference": "BALANCED",
  "aiEnabled": false,
  "language": "en"
}
```

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "id": 1,
    "userId": 1,
    "nickname": "Alex",
    "gender": "Prefer not to say",
    "birthDate": "2001-08-18",
    "birthTime": "09:30:00",
    "birthPlace": "Hong Kong",
    "birthTimezone": "Asia/Hong_Kong",
    "latitude": 22.3193,
    "longitude": 114.1694,
    "calendarType": "SOLAR",
    "preference": "BALANCED",
    "aiEnabled": false,
    "language": "en",
    "createdAt": "2026-05-03T16:31:00",
    "updatedAt": "2026-05-03T16:31:00"
  }
}
```

`language` is optional and currently accepts `en` or `zh`; invalid or blank values default to `en`.

`birthTimezone` should be a valid IANA timezone such as `Asia/Hong_Kong`. Bazi requires birth date, birth time, and timezone. Western astrology additionally requires latitude and longitude.

### GET `/api/profiles/{userId}`

Purpose: Load the user's profile.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response: same profile object as above.

### PUT `/api/profiles/{userId}`

Purpose: Update the user's profile.

Request example: same shape as profile creation.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response: updated profile object.

## Bazi

### POST `/api/bazi/generate/{userId}`

Purpose: Generate a Bazi result from the user's profile using `6tail/lunar-java`.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "id": 1,
    "userId": 1,
    "yearPillar": "辛巳 (Xin-Si)",
    "monthPillar": "壬申 (Ren-Shen)",
    "dayPillar": "癸卯 (Gui-Mao)",
    "hourPillar": "戊巳 (Wu-Si)",
    "woodScore": 45,
    "fireScore": 60,
    "earthScore": 45,
    "metalScore": 45,
    "waterScore": 60,
    "elementScores": {
      "Wood": 45,
      "Fire": 60,
      "Earth": 45,
      "Metal": 45,
      "Water": 60
    },
    "conclusion": "This simplified profile suggests...",
    "evidence": "The four pillars are...",
    "suggestion": "You may benefit from...",
    "chartJson": "{\"engine\":\"6tail/lunar-java 1.7.7\"}",
    "calculationMethod": "6tail/lunar-java 1.7.7, exact LiChun/JieQi pillar mode, element-weight-v1",
    "createdAt": "2026-05-03T16:32:00"
  }
}
```

### GET `/api/bazi/latest/{userId}`

Purpose: Load the latest generated Bazi result.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response: same Bazi result shape.

## Zodiac

### GET `/api/zodiac/daily/{userId}`

Purpose: Generate or load today's Western astrology daily board. The backend calculates the user's natal chart and the day's transit positions with Swiss Ephemeris, then scores selected personal-planet aspects.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "id": 1,
    "userId": 1,
    "zodiacSign": "Leo",
    "date": "2026-05-03",
    "emotionScore": 81,
    "communicationScore": 74,
    "actionScore": 88,
    "suggestion": "Today may support action more naturally...",
    "chartJson": "{\"method\":\"Swiss Ephemeris Java port...\"}",
    "calculationMethod": "Swiss Ephemeris Java port, bundled sepl_18/semo_18 data, Placidus houses, transit-aspect-v1",
    "createdAt": "2026-05-03T16:33:00"
  }
}
```

### GET `/api/zodiac/natal/{userId}`

Purpose: Return the user's structured natal chart from Swiss Ephemeris.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "engine": "Swiss Ephemeris Java port 2.01.00",
    "ephemerisMode": "SWIEPH bundled sepl_18/semo_18",
    "birthInstantUtc": "2001-08-18T01:30:00Z",
    "sunSign": "Leo",
    "ascendantSign": "Libra",
    "houseCusps": [185.1],
    "planets": {
      "Sun": {
        "longitude": 145.15,
        "sign": "Leo",
        "degreeInSign": 25.15,
        "retrograde": false,
        "house": 11
      }
    }
  }
}
```

### POST `/api/zodiac/match`

Purpose: Compare the user's astrology profile with a target person's birth date and optional MBTI-style personality tag. If the target only provides a date, the backend uses the target's noon Sun longitude. If target birth time, timezone, latitude, and longitude are all provided, the backend calculates a target natal chart and uses major personal-planet synastry aspects.

Headers: requires `Authorization: Bearer <accessToken>` for `request.userId`.

Request example:

```json
{
  "userId": 1,
  "targetNickname": "Jamie",
  "targetBirthDate": "2002-02-14",
  "targetBirthTime": "13:20:00",
  "targetBirthTimezone": "Asia/Hong_Kong",
  "targetLatitude": 22.3193,
  "targetLongitude": 114.1694,
  "targetPersonalityTag": "INFP"
}
```

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "targetNickname": "Jamie",
    "targetZodiacSign": "Aquarius",
    "zodiacScore": 80,
    "personalityScore": 70,
    "finalScore": 76,
    "level": "Good",
    "communicationTips": "This pairing may work best...",
    "riskNotes": "Watch for small assumptions...",
    "collaborationMode": "Balanced collaboration"
  }
}
```

## Assessments

### GET `/api/assessments/types`

Purpose: List assessment types.

Headers: requires `Authorization: Bearer <accessToken>`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": ["BFI10", "MBTI", "ATTACHMENT"]
}
```

### GET `/api/assessments/types/{userId}`

Purpose: List assessment types with localized display metadata from the user's profile language.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": [
    {
      "type": "BFI10",
      "displayName": "IPIP Big Five-20",
      "description": "A 20-item Big Five reflection based on public-domain IPIP-style item families."
    }
  ]
}
```

### GET `/api/assessments/{type}/questions`

Purpose: Load questions for `BFI10`, `MBTI`, or `ATTACHMENT`.

Headers: requires `Authorization: Bearer <accessToken>`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": [
    {
      "id": 1,
      "type": "BFI10",
      "instrumentVersion": "IPIP-BIG5-20-v1",
      "itemKey": "IPIP-EXT-1",
      "questionText": "Am the life of the party.",
      "dimension": "Extraversion",
      "reverseScore": false,
      "displayOrder": 1,
      "sourceNote": "IPIP public-domain item family; app-local zh translation is not normed"
    }
  ]
}
```

### GET `/api/assessments/{type}/questions/{userId}`

Purpose: Load questions using the user's profile language when available. If the user's language is `zh`, the backend returns the Chinese question text in `questionText`; otherwise it returns English.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": [
    {
      "id": 1,
      "questionText": "I see myself as someone who is reserved.",
      "dimension": "Extraversion",
      "reverseScore": true
    }
  ]
}
```

### POST `/api/assessments/{type}/submit`

Purpose: Submit questionnaire answers. Scores must be from 1 to 5. Every seeded question for the selected assessment type must be answered exactly once.

Assessments require a valid `userId`, but they do not require a completed birth profile. If a profile exists, its language controls the generated result label and summary; otherwise the backend defaults to English.

Headers: requires `Authorization: Bearer <accessToken>` for `request.userId`.

Request example:

```json
{
  "userId": 1,
  "answers": [
    {
      "questionId": 1,
      "score": 3
    },
    {
      "questionId": 2,
      "score": 4
    }
  ]
}
```

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "id": 1,
    "userId": 1,
    "type": "MBTI",
    "instrumentVersion": "MBTI-STYLE-32-v1",
    "resultLabel": "ENFP",
    "resultJson": "{\"scores\":{\"E/I:E\":4.5},\"details\":{\"instrument\":\"MBTI-STYLE-32-v1\"}}",
    "scores": {
      "E/I:E": 4.5
    },
    "summary": "Your MBTI-style result suggests ENFP...",
    "createdAt": "2026-05-03T16:35:00"
  }
}
```

### GET `/api/assessments/results/{userId}`

Purpose: Load all assessment results for the user, newest first.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response: list of assessment result objects.

## Dashboard

### GET `/api/dashboard/{userId}`

Purpose: Load the integrated dashboard. The backend auto-generates missing Bazi and today's Zodiac result when the profile exists.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "profile": {},
    "bazi": {},
    "zodiacDaily": {},
    "assessmentResults": [],
    "latestAiReport": {
      "id": 1,
      "userId": 1,
      "reportText": "# Integrated Insight\n## Conclusion\n...",
      "generatedBy": "QWEN",
      "createdAt": "2026-05-03T16:36:00"
    },
    "integratedSummary": "For Alex, the current profile may combine..."
  }
}
```

## AI Reports

### POST `/api/ai-reports/generate/{userId}`

Purpose: Generate an integrated report. Uses Qwen if configured; otherwise uses template fallback. The backend sends Qwen a compact report context instead of the full dashboard response; raw `chartJson`, prior report text, and `latestAiReport` are excluded from the prompt input.

If `QWEN_API_KEY` is missing, the endpoint returns a local `TEMPLATE` report. If `QWEN_API_KEY` is configured but the provider call fails or returns empty content, the endpoint returns an upstream error instead of silently falling back to `TEMPLATE`.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response field note: `sourceSnapshotJson` is stored in the backend database for generation/audit, but it is intentionally not returned by the report APIs or by `dashboard.latestAiReport`.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "id": 1,
    "userId": 1,
    "reportText": "# Integrated Insight\n## Conclusion\n...",
    "generatedBy": "QWEN",
    "createdAt": "2026-05-03T16:36:00"
  }
}
```

### GET `/api/ai-reports/latest/{userId}`

Purpose: Load the latest generated AI/template report.

Request body: none.

Headers: requires `Authorization: Bearer <accessToken>` for the same `userId`.

Response: same AI report shape.

## AI Chat

### POST `/api/ai/chat`

Purpose: Send one chat message to the backend AI assistant. The backend builds the system prompt from the same compact InsightSelf context used by AI reports, then includes recent messages from the requested chat session before calling Qwen when configured.

Headers: requires `Authorization: Bearer <accessToken>` for `request.userId`.

Request example:

```json
{
  "userId": 1,
  "message": "What should I focus on today?",
  "sessionId": null
}
```

`sessionId` is optional. Omit it or send `null` to start a new conversation. Send the returned `sessionId` in later requests to continue the same multi-turn session. An unknown session for the same user returns `400`. A request authenticated as another user returns `403` before the chat service can reuse the session.

Response example:

```json
{
  "success": true,
  "message": "ok",
  "data": {
    "reply": "Choose one small action today and observe how it affects your energy.",
    "sessionId": "b62ad63b-3f21-49a3-a5f2-3aa5a20d7fd1"
  }
}
```

If `QWEN_API_KEY` is missing, the endpoint returns a readable service-not-configured reply instead of calling the provider. User and assistant messages are still stored in the session so the API shape remains stable.
