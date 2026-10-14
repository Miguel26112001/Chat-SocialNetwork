-- =====================================================================
-- Contexto: NOTIFICATIONS  (esquema: notifications)
-- ---------------------------------------------------------------------
-- Responsabilidad: bandeja de avisos por interacciones de terceros.
-- Lenguaje ubicuo: notificación, bandeja, marcado como leído.
--
-- Es downstream puro: NO conoce a ningún otro contexto. Recibe eventos
-- (UserFollowed, PostLiked, CommentCreated, CommentReplied) y traduce
-- cada uno a una fila. Los UUIDs se guardan tal cual como evidencia
-- del evento, sin FK: si el origen desaparece, la notificación puede
-- quedar como "huérfana" y el módulo la oculta o purga por evento.
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.notifications (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID         NOT NULL,   -- → identity.users.id (sin FK: contexto externo)
    actor_id     UUID         NOT NULL,   -- → identity.users.id (sin FK: contexto externo)
    post_id      UUID         NULL,       -- → content.posts.id  (sin FK: contexto externo)
    type         VARCHAR(30)  NOT NULL,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_notifications_type      CHECK (type IN ('NEW_FOLLOW', 'POST_LIKE', 'POST_COMMENT', 'COMMENT_REPLY')),
    -- Regla de negocio: nadie se notifica a sí mismo.
    CONSTRAINT ck_notifications_not_self  CHECK (recipient_id <> actor_id)
);

-- Consulta típica: bandeja del usuario, sin leer primero.
CREATE INDEX ix_notifications_recipient ON notifications.notifications (recipient_id, is_read, created_at DESC);
CREATE INDEX ix_notifications_actor     ON notifications.notifications (actor_id);
CREATE INDEX ix_notifications_post      ON notifications.notifications (post_id);
