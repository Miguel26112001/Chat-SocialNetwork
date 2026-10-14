-- =====================================================================
-- Contexto: CONTENT / PUBLISHING  (esquema: content)
-- ---------------------------------------------------------------------
-- Responsabilidad: crear, publicar y consultar publicaciones.
-- Lenguaje ubicuo: publicación, visibilidad, multimedia, timeline.
--
-- Agregado: Post (raíz) con PostMedia como entidad de valor/hija.
-- post_media → posts es la ÚNICA relación con cascade en este esquema,
-- porque ambas tablas pertenecen al mismo contexto.
--
-- Dependencias (sin FK, por diseño estricto):
--   - user_id → identity.users      (autor)
--   - visibilidad FOLLOWERS requiere el puerto social.isFollowing()
--   - profiles.is_private afecta quién puede ver el timeline
-- El borrado de un post debe orquestarse en la aplicación: Content
-- publica PostDeleted y Engagement borra sus comentarios/likes.
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS content;

-- ---------------------------------------------------------------------
-- posts
-- Regla de negocio (capa de servicio): una publicación debe tener
-- contenido O al menos un archivo multimedia. No se puede validar de
-- forma fiable en SQL porque cruza dos tablas del mismo agregado.
-- ---------------------------------------------------------------------
CREATE TABLE content.posts (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,      -- → identity.users.id (sin FK: contexto externo)
    content    TEXT         NULL,          -- puede ser NULL si tiene multimedia
    visibility VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_content_posts_visibility CHECK (visibility IN ('PUBLIC', 'FOLLOWERS', 'PRIVATE'))
);

CREATE OR REPLACE FUNCTION content.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_content_posts_updated_at
    BEFORE UPDATE ON content.posts
    FOR EACH ROW
    EXECUTE FUNCTION content.set_updated_at();

CREATE INDEX ix_content_posts_user_id      ON content.posts (user_id);
CREATE INDEX ix_content_posts_user_created ON content.posts (user_id, created_at DESC);
CREATE INDEX ix_content_posts_visibility   ON content.posts (visibility);

-- ---------------------------------------------------------------------
-- post_media: archivos asociados a una publicación.
-- Se guarda la referencia al objeto en almacenamiento, no el binario.
-- ---------------------------------------------------------------------
CREATE TABLE content.post_media (
    id         UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id    UUID          NOT NULL,     -- → content.posts.id (mismo contexto: sí hay FK)
    media_url  VARCHAR(1000) NOT NULL,
    media_type VARCHAR(20)   NOT NULL,
    position   INTEGER       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_content_post_media_type     CHECK (media_type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT ck_content_post_media_position CHECK (position >= 0),
    CONSTRAINT fk_content_post_media_post
        FOREIGN KEY (post_id) REFERENCES content.posts (id) ON DELETE CASCADE
);

CREATE INDEX ix_content_post_media_post_id ON content.post_media (post_id);
