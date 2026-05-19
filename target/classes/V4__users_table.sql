-- V4: Create users table and add owner columns to music_bands
-- Created: 2026-04-05

-- Create users table with proper columns
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add owner columns to music_bands (if not exists)
ALTER TABLE music_bands ADD COLUMN owner_login VARCHAR(50);
ALTER TABLE music_bands ADD COLUMN owner_password_hash VARCHAR(100);

-- Index for faster ownership lookups
CREATE INDEX IF NOT EXISTS idx_music_bands_owner ON music_bands(owner_login);
