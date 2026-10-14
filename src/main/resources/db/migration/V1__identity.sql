-- =====================================================================
-- Contexto: IDENTITY & ACCESS  (esquema: identity)
-- ---------------------------------------------------------------------
-- Responsabilidad: registro de cuentas, credenciales y sesiones.
-- Lenguaje ubicuo: registro, login, sesión, revocar token, desactivar.
--
-- Es el "upstream universal" del sistema: el UUID de usuario que emite
-- esta tabla es el lenguaje compartido con el resto de contextos.
--
-- REGLA DE MODULARIDAD: este esquema NO referencia (ni es referenciado
-- con FK) por otros esquemas. Las demás tablas guardan el UUID como
-- columna simple y la validación/integridad se hace en la capa de
-- servicio de cada módulo, igual que en microservicios reales.
-- =====================================================================
CREATE SCHEMA IF NOT EXISTS identity;

-- ---------------------------------------------------------------------
-- users: SOLO credenciales y estado de cuenta.
-- El perfil (display_name, bio, avatar, is_private) vive en profiles.
-- ---------------------------------------------------------------------
CREATE TABLE identity.users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,   -- BCrypt/Argon2, nunca texto plano
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_identity_users_username UNIQUE (username),
    CONSTRAINT uq_identity_users_email    UNIQUE (email)
);

CREATE OR REPLACE FUNCTION identity.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_identity_users_updated_at
    BEFORE UPDATE ON identity.users
    FOR EACH ROW
    EXECUTE FUNCTION identity.set_updated_at();

-- ---------------------------------------------------------------------
-- refresh_tokens: sesiones persistentes y renovación de JWT.
-- Solo se guarda el HASH del token, nunca el token en texto plano.
-- ---------------------------------------------------------------------
CREATE TABLE identity.refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,      -- → identity.users.id (mismo contexto: sí hay FK)
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ  NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_identity_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_identity_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES identity.users (id) ON DELETE CASCADE
);

CREATE INDEX ix_identity_refresh_tokens_user   ON identity.refresh_tokens (user_id);
-- Sesiones activas de un usuario (excluye revocados): uso frecuente.
CREATE INDEX ix_identity_refresh_tokens_active ON identity.refresh_tokens (user_id)
    WHERE revoked_at IS NULL;
