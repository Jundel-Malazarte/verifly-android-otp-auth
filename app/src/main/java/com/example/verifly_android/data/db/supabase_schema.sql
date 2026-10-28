-- ================================================================
-- VERIFLY ANDROID - SUPABASE DATABASE SCHEMA SCRIPT
-- ================================================================

-- 1. DROP EXISTING TABLES IF RE-CREATING
DROP TABLE IF EXISTS public.customer_checklists CASCADE;
DROP TABLE IF EXISTS public.customers CASCADE;
DROP TABLE IF EXISTS public.users CASCADE;

-- 2. USERS TABLE
CREATE TABLE public.users (
    id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE,
    phone_number TEXT,
    address TEXT,
    is_verified BOOLEAN DEFAULT false,
    created_at BIGINT DEFAULT extract(epoch from now())::bigint * 1000
);

-- 3. CUSTOMERS TABLE
CREATE TABLE public.customers (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT,
    phone TEXT,
    address TEXT,
    status TEXT DEFAULT 'Active',
    notes TEXT,
    created_at BIGINT DEFAULT extract(epoch from now())::bigint * 1000
);

-- 4. CUSTOMER CHECKLISTS TABLE
CREATE TABLE public.customer_checklists (
    id TEXT PRIMARY KEY,
    customer_id TEXT,
    title TEXT NOT NULL,
    category TEXT DEFAULT 'General',
    is_completed BOOLEAN DEFAULT false,
    created_at BIGINT DEFAULT extract(epoch from now())::bigint * 1000
);

-- 5. FAST QUERY INDEXES
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON public.users(phone_number);
CREATE INDEX IF NOT EXISTS idx_customers_email ON public.customers(email);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON public.customers(phone);

-- 6. DISABLE RLS FOR ANONYMOUS REST API ACCESS
ALTER TABLE public.users DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.customers DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.customer_checklists DISABLE ROW LEVEL SECURITY;