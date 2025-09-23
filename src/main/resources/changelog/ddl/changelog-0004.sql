ALTER TABLE accounts DROP CONSTRAINT accounts_role_check;

ALTER TABLE accounts
    ADD CONSTRAINT accounts_role_check
        CHECK (role IN ('USER', 'ADMIN', 'DOCTOR'));