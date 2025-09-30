ALTER TABLE public.transactions
DROP CONSTRAINT chk_payment_status;

ALTER TABLE public.transactions
    ADD CONSTRAINT chk_payment_status
        CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED'));

-- Thêm cột refund_status kèm constraint
ALTER TABLE public.transactions
    ADD COLUMN refund_status VARCHAR(20) NOT NULL DEFAULT 'NONE'
        CHECK (refund_status IN ('NONE', 'PROCESSING', 'COMPLETED', 'FAILED'));

-- Thêm cột zp_refund_id để lưu refundId từ ZaloPay
ALTER TABLE public.transactions
    ADD COLUMN zp_refund_id VARCHAR(100);