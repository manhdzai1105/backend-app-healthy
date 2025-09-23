-- Thêm cột device_id
ALTER TABLE public.device_tokens
    ADD COLUMN device_id VARCHAR(128);

ALTER TABLE public.device_tokens
    ALTER COLUMN device_id SET NOT NULL;

ALTER TABLE public.device_tokens
    ADD CONSTRAINT uq_user_device UNIQUE (user_id, device_id);

ALTER TABLE public.keys
    DROP CONSTRAINT IF EXISTS keys_user_id_key;

ALTER TABLE public.keys
    ADD COLUMN device_id VARCHAR(128);

ALTER TABLE public.keys
    ALTER COLUMN device_id SET NOT NULL;

ALTER TABLE public.keys
    ADD CONSTRAINT uq_keys_user_device UNIQUE (user_id, device_id);