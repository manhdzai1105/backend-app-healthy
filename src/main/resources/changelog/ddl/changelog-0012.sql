-- Drop constraint NOT NULL cho password
ALTER TABLE public.accounts
    ALTER COLUMN password DROP NOT NULL;
