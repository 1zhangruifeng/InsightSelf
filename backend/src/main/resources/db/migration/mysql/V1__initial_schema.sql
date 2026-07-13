CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE auth_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    access_token_hash VARCHAR(64) NOT NULL,
    refresh_token_hash VARCHAR(64) NOT NULL,
    access_token_expires_at DATETIME(6) NOT NULL,
    refresh_token_expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6),
    revoked_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY idx_auth_sessions_access_token_hash (access_token_hash),
    UNIQUE KEY idx_auth_sessions_refresh_token_hash (refresh_token_hash),
    KEY idx_auth_sessions_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    gender VARCHAR(64),
    birth_date DATE NOT NULL,
    birth_time TIME,
    birth_place VARCHAR(128),
    birth_timezone VARCHAR(64),
    latitude DOUBLE,
    longitude DOUBLE,
    calendar_type VARCHAR(16) NOT NULL,
    preference VARCHAR(16) NOT NULL,
    ai_enabled BOOLEAN NOT NULL,
    language VARCHAR(8) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_profiles_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bazi_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    year_pillar VARCHAR(255),
    month_pillar VARCHAR(255),
    day_pillar VARCHAR(255),
    hour_pillar VARCHAR(255),
    wood_score INT NOT NULL,
    fire_score INT NOT NULL,
    earth_score INT NOT NULL,
    metal_score INT NOT NULL,
    water_score INT NOT NULL,
    chart_json MEDIUMTEXT,
    calculation_method VARCHAR(128),
    conclusion VARCHAR(1200),
    evidence VARCHAR(1200),
    suggestion VARCHAR(1200),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE zodiac_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    zodiac_sign VARCHAR(255) NOT NULL,
    insight_date DATE NOT NULL,
    emotion_score INT NOT NULL,
    communication_score INT NOT NULL,
    action_score INT NOT NULL,
    chart_json MEDIUMTEXT,
    calculation_method VARCHAR(128),
    suggestion VARCHAR(1200),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE assessment_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(32) NOT NULL,
    instrument_version VARCHAR(32) NOT NULL,
    item_key VARCHAR(64) NOT NULL,
    question_text VARCHAR(500) NOT NULL,
    question_text_zh VARCHAR(500),
    dimension VARCHAR(64) NOT NULL,
    reverse_score BOOLEAN NOT NULL,
    display_order INT NOT NULL,
    source_note VARCHAR(256),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE assessment_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    instrument_version VARCHAR(32) NOT NULL,
    result_label VARCHAR(64) NOT NULL,
    result_json MEDIUMTEXT NOT NULL,
    summary VARCHAR(1200),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_text MEDIUMTEXT NOT NULL,
    source_snapshot_json MEDIUMTEXT NOT NULL,
    generated_by VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_chat_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ai_chat_sessions_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(16) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
