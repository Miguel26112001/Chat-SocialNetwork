-- =====================================================================
-- Contexto: SOCIAL GRAPH  (esquema: social)
-- ---------------------------------------------------------------------
-- Responsabilidad: relaciones de seguimiento entre usuarios.
-- Lenguaje ubicuo: seguir, dejar de seguir, seguidor, siguiendo,
--                  solicitud pendiente (futuro: follow_requests).
--
-- Publica el puerto/published language:
--     isFollowing(follower, following) -> boolean
--     followersOf(user) / followingOf(user)
-- que Content usa para resolver la visibilidad FOLLOWERS.
--
-- follower_id y following_id apuntan a identity.users, pero sin FK:
-- siguiendo la estrategia estricta, la elimación de un usuario debe
-- orquestarse en la aplicación (borrar follows del usuario = evento
-- UserDeleted consumido por Social Graph).
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS social;

CREATE TABLE social.follows (
    follower_id  UUID        NOT NULL,  -- → identity.users.id (sin FK: contexto externo)
    following_id UUID        NOT NULL,  -- → identity.users.id (sin FK: contexto externo)
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, following_id),
    -- Regla de negocio: un usuario no puede seguirse a sí mismo.
    CONSTRAINT ck_social_follows_not_self CHECK (follower_id <> following_id)
);

-- Para búsquedas inversas: "¿quién me sigue?"
CREATE INDEX ix_social_follows_following_id ON social.follows (following_id);
