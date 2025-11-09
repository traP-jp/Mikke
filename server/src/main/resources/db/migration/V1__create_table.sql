CREATE TABLE user_sessions
(
    session_id VARCHAR(255) PRIMARY KEY,
    value      TEXT NOT NULL,
    expire_at  LONG NOT NULL
);
