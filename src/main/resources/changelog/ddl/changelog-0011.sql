ALTER TABLE public.notifications
DROP CONSTRAINT notifications_type_check;

ALTER TABLE public.notifications
    ADD CONSTRAINT notifications_type_check
        CHECK (type IN (
            'APPOINTMENT_PENDING',
            'APPOINTMENT_CONFIRMED',
            'APPOINTMENT_CANCELLED',
            'APPOINTMENT_RESCHEDULED',
            'APPOINTMENT_REVIEW_REQUEST',
            'REMINDER'
));
