-- =====================================================================
-- V7: roles de cuenta (USER común / ADMIN).
-- ---------------------------------------------------------------------
-- Toda cuenta existente nace como USER. La promoción a ADMIN es una
-- operación explícita posterior (promoteToAdmin del agregado).
-- =====================================================================

ALTER TABLE identity.users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

ALTER TABLE identity.users
    ADD CONSTRAINT ck_identity_users_role CHECK (role IN ('USER', 'ADMIN'));
