-- =====================================================================
-- V8: bloqueo temporal por intentos fallidos de login.
-- ---------------------------------------------------------------------
-- failed_login_attempts cuenta los fallos consecutivos (se reinicia
-- con un login exitoso). locked_until marca hasta cuándo está
-- bloqueada la cuenta; NULL = no bloqueada.
-- =====================================================================

ALTER TABLE identity.users
    ADD COLUMN failed_login_attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE identity.users
    ADD COLUMN locked_until TIMESTAMPTZ NULL;

ALTER TABLE identity.users
    ADD CONSTRAINT ck_identity_users_failed_attempts CHECK (failed_login_attempts >= 0);
