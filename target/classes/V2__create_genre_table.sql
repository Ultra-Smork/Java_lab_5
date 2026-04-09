-- V2: Create music_genres lookup table
-- Created: 2026-03-31

-- Create music_genres table
CREATE TABLE IF NOT EXISTS music_genres (
    id SERIAL PRIMARY KEY,
    genre_name VARCHAR(50) NOT NULL UNIQUE
);

-- Insert genre values
INSERT INTO music_genres (genre_name) VALUES 
    ('PSYCHEDELIC_ROCK'),
    ('MATH_ROCK'),
    ('POST_ROCK')
ON CONFLICT (genre_name) DO NOTHING;

-- Add genre_id column to music_bands (nullable for backward compatibility)
ALTER TABLE music_bands ADD COLUMN IF NOT EXISTS genre_id INTEGER REFERENCES music_genres(id);

-- Migrate existing data from genre string to genre_id
UPDATE music_bands mb
SET genre_id = mg.id
FROM music_genres mg
WHERE mb.genre = mg.genre_name;

-- Make genre_id NOT NULL for new inserts (after migration)
-- Note: Existing rows with null genre will remain as is