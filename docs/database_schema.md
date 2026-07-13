# Database Schema

The backend schema is managed by Flyway migrations, not Hibernate auto-DDL. Runtime persistence uses SQLite by default through `jdbc:sqlite:./insightself.sqlite`; MySQL remains available through the `mysql` Spring profile; integration tests use H2 in memory. Migration scripts live under `db/migration/sqlite`, `db/migration/mysql`, and `db/migration/h2`.

## `users`

Purpose: Stores demo user accounts.

Fields:

- `id`: primary key
- `username`: unique username
- `password_hash`: Spring Security delegating password hash such as bcrypt; legacy 64-character SHA-256 hashes are migrated after successful login or password change
- `created_at`: account creation time
- `updated_at`: last account update time

## `auth_sessions`

Purpose: Stores server-side bearer sessions. The client receives random access and refresh tokens, but the database stores only SHA-256 token digests.

Fields:

- `id`: primary key
- `user_id`: owner user id linked to `users.id`
- `access_token_hash`: unique SHA-256 digest of the random access token
- `refresh_token_hash`: unique SHA-256 digest of the random refresh token
- `access_token_expires_at`: access token expiration time
- `refresh_token_expires_at`: refresh token expiration time
- `created_at`: session creation time
- `last_used_at`: last successful access-token authentication time
- `revoked_at`: non-null when the session has been revoked

## `user_profiles`

Purpose: Stores the unified profile used by all modules.

Fields:

- `id`: primary key
- `user_id`: unique user id linked to `users.id`
- `nickname`: display name
- `gender`: optional free-text gender field
- `birth_date`: date of birth
- `birth_time`: time of birth
- `birth_place`: free-text birth place
- `birth_timezone`: optional IANA timezone, required by Bazi and Western astrology calculation
- `latitude`: optional latitude, required by Western astrology calculation
- `longitude`: optional longitude, required by Western astrology calculation
- `calendar_type`: `SOLAR` or `LUNAR`
- `preference`: `EASTERN`, `WESTERN`, or `BALANCED`
- `ai_enabled`: whether the user enabled AI synthesis in the profile
- `language`: UI/content language preference, currently `en` or `zh`
- `created_at`: profile creation time
- `updated_at`: last update time

## `bazi_results`

Purpose: Stores generated Bazi analysis results calculated through `6tail/lunar-java`.

Fields:

- `id`: primary key
- `user_id`: user id
- `year_pillar`: year pillar display text
- `month_pillar`: month pillar display text
- `day_pillar`: day pillar display text
- `hour_pillar`: hour pillar display text
- `wood_score`: Wood score
- `fire_score`: Fire score
- `earth_score`: Earth score
- `metal_score`: Metal score
- `water_score`: Water score
- `chart_json`: structured calculation payload including input, solar/lunar dates, pillars, hidden stems, ten-gods context, na-yin, xun-kong, and element scores
- `calculation_method`: calculator and scoring method identifier
- `conclusion`: generated soft conclusion
- `evidence`: explanation of pillars and element scores
- `suggestion`: self-reflection suggestion
- `created_at`: generation time

## `zodiac_results`

Purpose: Stores daily Western astrology board results.

Fields:

- `id`: primary key
- `user_id`: user id
- `zodiac_sign`: Western sun sign
- `insight_date`: date for the daily insight
- `emotion_score`: daily emotion score
- `communication_score`: daily communication score
- `action_score`: daily action score
- `chart_json`: structured Swiss Ephemeris payload including natal chart, daily transit chart, and aspect scoring details
- `calculation_method`: calculator and scoring method identifier
- `suggestion`: daily suggestion text
- `created_at`: generation time

## `assessment_questions`

Purpose: Stores seeded question banks for the API-compatible `BFI10`, `MBTI`, and `ATTACHMENT` type codes.

Fields:

- `id`: primary key
- `type`: `BFI10`, `MBTI`, or `ATTACHMENT`
- `instrument_version`: version string such as `IPIP-BIG5-20-v1`, `MBTI-STYLE-32-v1`, or `ECR-RS-GENERAL-9-v1`
- `item_key`: stable item key within the instrument
- `question_text`: question content
- `question_text_zh`: app-local Chinese translation when available
- `dimension`: scoring dimension
- `reverse_score`: whether score is reversed as `6 - score`
- `display_order`: display order in the Android questionnaire
- `source_note`: short source/provenance note for the item family

## `assessment_results`

Purpose: Stores submitted assessment results.

Fields:

- `id`: primary key
- `user_id`: user id
- `type`: `BFI10`, `MBTI`, or `ATTACHMENT`
- `instrument_version`: version of the question bank/scoring strategy used for this result
- `result_label`: result label such as `ENFP`, `Secure`, or a strongest Big Five dimension
- `result_json`: JSON string containing `scores` and structured `details`
- `summary`: readable summary
- `created_at`: submission time

## `ai_reports`

Purpose: Stores generated integrated reports.

Fields:

- `id`: primary key
- `user_id`: user id
- `report_text`: generated report text
- `source_snapshot_json`: compact JSON snapshot used for report generation. It contains profile preference, Bazi pillars/element scores, Zodiac daily scores, assessment summaries, and integrated summary. It intentionally excludes raw `chartJson`, prior `sourceSnapshotJson`, `latestAiReport`, and prior report text. This is an internal server-side generation/audit field and is not returned by the Android-facing report APIs.
- `generated_by`: `QWEN` or `TEMPLATE`
- `created_at`: generation time

## `ai_chat_sessions`

Purpose: Stores backend chat sessions so AI chat can continue a multi-turn conversation.

Fields:

- `id`: primary key
- `session_id`: public UUID returned to the Android client
- `user_id`: owner user id
- `created_at`: session creation time
- `updated_at`: last message time

## `ai_chat_messages`

Purpose: Stores user and assistant turns for recent-history prompting.

Fields:

- `id`: primary key
- `session_id`: chat session UUID
- `user_id`: owner user id
- `role`: `user` or `assistant`
- `content`: message text
- `created_at`: message time

The Qwen prompt includes only the most recent 12 stored messages for a session, plus the current user message.
