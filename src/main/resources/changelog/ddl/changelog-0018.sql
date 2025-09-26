ALTER TABLE public.transactions
DROP CONSTRAINT chk_payment_status;

ALTER TABLE public.transactions
    ADD CONSTRAINT chk_payment_status
        CHECK (payment_status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'REFUND_PROCESSING'));
