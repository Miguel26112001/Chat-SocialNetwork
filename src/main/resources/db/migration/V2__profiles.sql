-- =====================================================================
-- Contexto: USER PROFILE  (esquema: profiles)
-- ---------------------------------------------------------------------
-- Responsabilidad: cómo se presenta el usuario en la red social.
-- Lenguaje ubicuo: displayName, biografía, avatar, cuenta privada.
--
-- Relación con Identity: 1:1. Se crea cuando llega el evento de
-- dominio UserRegistered (publicado por Identity). NO hay FK hacia
-- identity.users: la consistencia se restaura en la aplicación
-- (patrón "crear perfil al registrarse" + limpieza por evento
-- UserDeactivated/UserDeleted).
--
-- is_private NO vive aquí como simple preferencia: Profile lo publica
-- como parte de su "published language" y Social Graph y Content lo
-- consultan para autorizar seguidores y visibilidad.
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS profiles;

CREATE TABLE profiles.profiles (
    user_id      UUID         PRIMARY KEY,  -- → identity.users.id (sin FK: contexto externo)
    display_name VARCHAR(100) NOT NULL,
    bio          TEXT         NULL,
    avatar_url   VARCHAR(500) NULL,
    is_private   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION profiles.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON profiles.profiles
    FOR EACH ROW
    EXECUTE FUNCTION profiles.set_updated_at();
