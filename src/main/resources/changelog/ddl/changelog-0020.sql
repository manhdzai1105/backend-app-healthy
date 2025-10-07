-- 🧩 Xoá ràng buộc cũ
ALTER TABLE public.messages
DROP CONSTRAINT IF EXISTS messages_message_type_check;

-- 🧩 Thêm ràng buộc mới
ALTER TABLE public.messages
    ADD CONSTRAINT messages_message_type_check
        CHECK (message_type IN ('MESSAGE', 'FILE', 'IMAGE', 'VIDEO', 'AUDIO'));
