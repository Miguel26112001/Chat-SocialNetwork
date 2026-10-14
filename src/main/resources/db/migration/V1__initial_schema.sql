-- =====================================================================
-- V1: Esquema inicial del Chat Social
-- Requiere PostgreSQL 13+ (gen_random_uuid() está incluido desde la 13).
-- Orden de creación respetando las dependencias entre tablas.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Función auxiliar: mantiene updated_at automáticamente.
-- Se aplica con triggers en las tablas que tienen columna updated_at.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================================
-- 1. users
-- =====================================================================
CREATE TABLE users (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50)   NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,   -- BCrypt/Argon2, nunca texto plano
    display_name  VARCHAR(100)  NOT NULL,
    bio           TEXT          NULL,
    avatar_url    VARCHAR(500)  NULL,
    is_private    BOOLEAN       NOT NULL DEFAULT FALSE,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- 2. posts
-- =====================================================================
CREATE TABLE posts (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content    TEXT          NULL,          -- puede ser NULL si tiene multimedia
    visibility VARCHAR(20)   NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_posts_visibility CHECK (visibility IN ('PUBLIC', 'FOLLOWERS', 'PRIVATE'))
);

CREATE INDEX ix_posts_user_id     ON posts (user_id);
CREATE INDEX ix_posts_user_created ON posts (user_id, created_at DESC);
CREATE INDEX ix_posts_visibility  ON posts (visibility);

CREATE TRIGGER trg_posts_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- 3. comments
--    La restricción de que el comentario padre pertenezca a la MISMA
--    publicación se garantiza a nivel de base de datos mediante una FK
--    compuesta (parent_comment_id, post_id) -> (id, post_id).
--    Si parent_comment_id es NULL, es un comentario principal.
-- =====================================================================
CREATE TABLE comments (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id           UUID          NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id           UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    parent_comment_id UUID          NULL,
    content           TEXT          NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_comments_id_post     UNIQUE (id, post_id),
    CONSTRAINT ck_comments_content_not_blank CHECK (length(trim(content)) > 0)
);

-- Se crea después de la tabla para poder referenciarla a sí misma.
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_parent_same_post
    FOREIGN KEY (parent_comment_id, post_id)
    REFERENCES comments (id, post_id)
    ON DELETE CASCADE;

CREATE INDEX ix_comments_post_id ON comments (post_id);
CREATE INDEX ix_comments_user_id ON comments (user_id);
CREATE INDEX ix_comments_parent  ON comments (parent_comment_id);

CREATE TRIGGER trg_comments_updated_at
    BEFORE UPDATE ON comments
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- 4. post_likes
--    PK compuesta (user_id, post_id): impide likes duplicados.
-- =====================================================================
CREATE TABLE post_likes (
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    post_id    UUID        NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, post_id)
);

-- Necesario para búsquedas de "quién dio like a este post" y conteos.
CREATE INDEX ix_post_likes_post_id ON post_likes (post_id);

-- =====================================================================
-- 5. follows
--    PK compuesta (follower_id, following_id): impide seguimientos
--    duplicados. Un usuario no puede seguirse a sí mismo.
-- =====================================================================
CREATE TABLE follows (
    follower_id  UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    following_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT ck_follows_not_self CHECK (follower_id <> following_id)
);

-- Para búsquedas inversas: "¿quién me sigue?"
CREATE INDEX ix_follows_following_id ON follows (following_id);

-- =====================================================================
-- 6. post_media
-- =====================================================================
CREATE TABLE post_media (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id    UUID          NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    media_url  VARCHAR(1000) NOT NULL,     -- referencia al objeto en almacenamiento
    media_type VARCHAR(20)   NOT NULL,
    position   INTEGER       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_post_media_type     CHECK (media_type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT ck_post_media_position CHECK (position >= 0)
);

CREATE INDEX ix_post_media_post_id ON post_media (post_id);

-- =====================================================================
-- 7. notifications
-- =====================================================================
CREATE TABLE notifications (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    actor_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    post_id      UUID         NULL REFERENCES posts (id) ON DELETE CASCADE,
    type         VARCHAR(30)  NOT NULL,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_notifications_type CHECK (type IN ('NEW_FOLLOW', 'POST_LIKE', 'POST_COMMENT', 'COMMENT_REPLY')),
    CONSTRAINT ck_notifications_not_self CHECK (recipient_id <> actor_id)
);

-- Consulta típica: bandeja del usuario, sin leer primero.
CREATE INDEX ix_notifications_recipient ON notifications (recipient_id, is_read, created_at DESC);
CREATE INDEX ix_notifications_actor     ON notifications (actor_id);
CREATE INDEX ix_notifications_post      ON notifications (post_id);

-- =====================================================================
-- 8. refresh_tokens
--    Solo se guarda el HASH del token, nunca el token en texto plano.
-- =====================================================================
CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ  NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id);
-- Sesiones activas de un usuario (excluye revocados): uso frecuente.
CREATE INDEX ix_refresh_tokens_active  ON refresh_tokens (user_id) WHERE revoked_at IS NULL;
