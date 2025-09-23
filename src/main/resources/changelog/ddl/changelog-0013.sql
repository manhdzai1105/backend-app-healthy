-- Drop constraint NOT NULL cho email
ALTER TABLE public.accounts
    ALTER COLUMN email DROP NOT NULL;

-- Drop constraint UNIQUE
ALTER TABLE public.accounts
    DROP CONSTRAINT accounts_username_key;

-- Thêm cột provider (loại đăng nhập: LOCAL, GOOGLE, FACEBOOK)
ALTER TABLE public.accounts
    ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL'
        CHECK (auth_provider IN ('LOCAL', 'GOOGLE', 'FACEBOOK'));

-- Thêm cột provider_id (mã định danh từ provider, có thể NULL nếu login local)
ALTER TABLE public.accounts
    ADD COLUMN auth_provider_id VARCHAR(100);

-- Nếu muốn đảm bảo không có trùng lặp giữa các social ID
ALTER TABLE public.accounts
    ADD CONSTRAINT accounts_provider_provider_id_key UNIQUE (auth_provider, auth_provider_id);