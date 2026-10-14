-- =====================================================================
-- Contexto: ENGAGEMENT  (esquema: engagement)
-- ---------------------------------------------------------------------
-- Responsabilidad: reaccionar al contenido (comentarios y likes).
-- Lenguaje ubicuo: comentar, responder, dar like.
--
-- Publica eventos de dominio que Notifications consume:
--     PostLiked(postId, actorId, ownerId)
--     CommentCreated(postId, commentId, actorId, ownerId)
--     CommentReplied(postId, parentCommentId, actorId, parentIdOwner)
--
-- post_id y user_id apuntan a contextos externos (content e identity)
-- sin FK. Engagement valida en servicio que el post exista antes de
-- guardar un like/comentario (regla de negocio de la especificación).
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS engagement;

CREATE OR REPLACE FUNCTION engagement.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------
-- comments: comentarios de post y respuestas anidadas.
-- Si parent_comment_id es NULL → comentario principal.
-- La FK compuesta (parent_comment_id, post_id) → (id, post_id)
-- garantiza a nivel de base de datos que el comentario padre
-- pertenezca a la MISMA publicación (regla de la especificación).
-- ---------------------------------------------------------------------
CREATE TABLE engagement.comments (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id           UUID         NOT NULL,   -- → content.posts.id (sin FK: contexto externo)
    user_id           UUID         NOT NULL,   -- → identity.users.id (sin FK: contexto externo)
    parent_comment_id UUID         NULL,
    content           TEXT         NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_engagement_comments_id_post     UNIQUE (id, post_id),
    CONSTRAINT ck_engagement_comments_not_blank   CHECK (length(trim(content)) > 0)
);

ALTER TABLE engagement.comments
    ADD CONSTRAINT fk_engagement_comments_parent_same_post
    FOREIGN KEY (parent_comment_id, post_id)
    REFERENCES engagement.comments (id, post_id)
    ON DELETE CASCADE;

CREATE TRIGGER trg_engagement_comments_updated_at
    BEFORE UPDATE ON engagement.comments
    FOR EACH ROW
    EXECUTE FUNCTION engagement.set_updated_at();

CREATE INDEX ix_engagement_comments_post_id ON engagement.comments (post_id);
CREATE INDEX ix_engagement_comments_user_id ON engagement.comments (user_id);
CREATE INDEX ix_engagement_comments_parent  ON engagement.comments (parent_comment_id);

-- ---------------------------------------------------------------------
-- post_likes: PK compuesta → impide likes duplicados por usuario.
-- Relación N:M entre usuarios y publicaciones.
-- ---------------------------------------------------------------------
CREATE TABLE engagement.post_likes (
    user_id    UUID        NOT NULL,  -- → identity.users.id (sin FK: contexto externo)
    post_id    UUID        NOT NULL,  -- → content.posts.id  (sin FK: contexto externo)
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, post_id)
);

-- Para búsquedas de "quién dio like a este post" y conteos.
CREATE INDEX ix_engagement_post_likes_post_id ON engagement.post_likes (post_id);
