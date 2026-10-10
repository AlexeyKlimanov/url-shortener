CREATE TABLE links (
    id            BIGSERIAL PRIMARY KEY,
    short_code    VARCHAR(10)  NOT NULL UNIQUE,
    original_url  TEXT         NOT NULL,
    user_id       BIGINT       NOT NULL,
    click_count   BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_links_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_links_user_id ON links (user_id);