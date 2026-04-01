-- V1: Initial schema for music_bands table
-- Created: 2026-03-31

-- Create sequence for auto-incrementing IDs
CREATE SEQUENCE IF NOT EXISTS music_band_id_seq START WITH 1;

-- Create music_bands table
CREATE TABLE IF NOT EXISTS music_bands (
    id BIGINT PRIMARY KEY DEFAULT NEXTVAL('music_band_id_seq'),
    name VARCHAR(255) NOT NULL,
    x BIGINT,
    y INTEGER,
    creation_date TIMESTAMP,
    number_of_participants INTEGER NOT NULL,
    description TEXT,
    genre VARCHAR(50),
    album_name VARCHAR(255) NOT NULL,
    album_sales DOUBLE PRECISION NOT NULL
);

-- Create index on id column
CREATE INDEX IF NOT EXISTS idx_music_bands_id ON music_bands(id);